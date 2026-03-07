package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.model.SeatLock;
import com.jwtAuthentication.jwt.model.Show;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import com.jwtAuthentication.jwt.repository.SeatLockRepository;
import com.jwtAuthentication.jwt.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeatLockService {

    private static final int LOCK_DURATION_MINUTES = 5;

    @Autowired
    private SeatLockRepository seatLockRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Lock seats for the duration of a payment window (5 minutes).
     *
     * Checks:  1) No already-CONFIRMED booking covers these seats.
     *          2) No active (non-expired) seat lock covers these seats.
     *
     * The unique constraint on (show_id, seat_label) in seat_locks acts as the
     * last line of defence against concurrent duplicate inserts.
     *
     * @throws RuntimeException if any seat is already booked or locked
     */
    @Transactional
    public void lockSeats(Long showId, List<String> seatLabels, Long userId, String razorpayOrderId) {
        LocalDateTime now = LocalDateTime.now();

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found: " + showId));

        // ── 1. Check CONFIRMED bookings ────────────────────────────────────
        Set<String> bookedSeats = new HashSet<>();
        bookingRepository.findByShowShowId(showId).stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.CONFIRMED)
                .filter(b -> b.getSeatLabels() != null)
                .forEach(b -> Arrays.stream(b.getSeatLabels().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(bookedSeats::add));

        List<String> alreadyBooked = seatLabels.stream()
                .filter(bookedSeats::contains)
                .collect(Collectors.toList());

        if (!alreadyBooked.isEmpty()) {
            throw new RuntimeException("Seats already booked: " + String.join(", ", alreadyBooked));
        }

        // ── 2. Check active seat locks ─────────────────────────────────────
        List<SeatLock> activeLocks = seatLockRepository.findActiveLocksForSeats(showId, seatLabels, now);
        if (!activeLocks.isEmpty()) {
            List<String> lockedLabels = activeLocks.stream()
                    .map(SeatLock::getSeatLabel)
                    .collect(Collectors.toList());
            throw new RuntimeException(
                    "Seats are temporarily reserved by another user: " + String.join(", ", lockedLabels));
        }

        // ── 3. Insert locks ────────────────────────────────────────────────
        LocalDateTime lockedUntil = now.plusMinutes(LOCK_DURATION_MINUTES);
        for (String seatLabel : seatLabels) {
            SeatLock lock = new SeatLock();
            lock.setShow(show);
            lock.setSeatLabel(seatLabel);
            lock.setLockedByUserId(userId);
            lock.setRazorpayOrderId(razorpayOrderId);
            lock.setLockedUntil(lockedUntil);
            seatLockRepository.save(lock);
        }
    }

    /**
     * Release all seat locks tied to a Razorpay order.
     * Called after payment is confirmed or marked as failed.
     */
    @Transactional
    public void releaseLocksForOrder(String razorpayOrderId) {
        seatLockRepository.deleteByRazorpayOrderId(razorpayOrderId);
    }

    /**
     * Returns seat labels that are currently locked (not yet expired) for a show.
     */
    public List<String> getLockedSeatLabels(Long showId) {
        return seatLockRepository.findActiveLocksForShow(showId, LocalDateTime.now())
                .stream()
                .map(SeatLock::getSeatLabel)
                .collect(Collectors.toList());
    }

    /**
     * Scheduled cleanup: deletes expired seat locks every 5 minutes.
     * Prevents the seat_locks table from growing unbounded when users
     * abandon the payment modal without completing or explicitly cancelling.
     */
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void cleanupExpiredLocks() {
        seatLockRepository.deleteExpiredLocks(LocalDateTime.now());
    }
}
