package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expirePendingBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredCandidates = bookingRepository
                .findByPaymentStatusAndExpiresAtBefore(Booking.PaymentStatus.PENDING, now);

        if (expiredCandidates.isEmpty()) {
            return;
        }

        for (Booking booking : expiredCandidates) {
            booking.setPaymentStatus(Booking.PaymentStatus.EXPIRED);
            bookingRepository.save(booking);

            if (booking.getRazorpayOrderId() != null && !booking.getRazorpayOrderId().isBlank()) {
                seatLockService.releaseLocksForOrder(booking.getRazorpayOrderId());
            }
        }

        log.info("Expired {} pending bookings at {}", expiredCandidates.size(), now);
    }
}
