package com.jwtAuthentication.jwt.controller;

import com.jwtAuthentication.jwt.DTO.SeatStatusDTO;
import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import com.jwtAuthentication.jwt.service.SeatLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatLockService seatLockService;

    /**
     * Returns all CONFIRMED seat labels for a given show.
     * Frontend uses this to mark sold seats as unselectable.
     *
     * GET /bookings/show/{showId}/booked-seats
     */
    @GetMapping("/show/{showId}/booked-seats")
    public ResponseEntity<List<String>> getBookedSeats(@PathVariable Long showId) {
        List<String> bookedSeats = bookingRepository.findByShowShowId(showId).stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.CONFIRMED)
                .filter(b -> b.getSeatLabels() != null)
                .flatMap(b -> Arrays.stream(b.getSeatLabels().split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookedSeats);
    }

    /**
     * Returns the status of every non-available seat for a show.
     * Only BOOKED and LOCKED seats are included; all others are implicitly AVAILABLE.
     *
     * Response shape: [ { "seatLabel": "A1", "status": "BOOKED" }, ... ]
     *
     * GET /bookings/show/{showId}/seat-status
     */
    @GetMapping("/show/{showId}/seat-status")
    public ResponseEntity<List<SeatStatusDTO>> getSeatStatus(@PathVariable Long showId) {

        // Collect CONFIRMED seat labels
        Set<String> bookedSeats = bookingRepository.findByShowShowId(showId).stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.CONFIRMED)
                .filter(b -> b.getSeatLabels() != null)
                .flatMap(b -> Arrays.stream(b.getSeatLabels().split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // Active lock labels, excluding seats that are already CONFIRMED (lock would be stale)
        List<String> lockedSeats = seatLockService.getLockedSeatLabels(showId).stream()
                .filter(label -> !bookedSeats.contains(label))
                .collect(Collectors.toList());

        List<SeatStatusDTO> result = new ArrayList<>();
        bookedSeats.forEach(label -> result.add(new SeatStatusDTO(label, "BOOKED")));
        lockedSeats.forEach(label -> result.add(new SeatStatusDTO(label, "LOCKED")));

        return ResponseEntity.ok(result);
    }
}
