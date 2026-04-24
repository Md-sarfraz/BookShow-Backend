package com.jwtAuthentication.jwt.controller;

import com.jwtAuthentication.jwt.DTO.SeatStatusDTO;
import com.jwtAuthentication.jwt.DTO.requestDto.CancelBookingRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.CancelBookingResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.CancellationPreviewResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.RefundHistoryItemDto;
import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.model.Refund;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import com.jwtAuthentication.jwt.repository.RefundRepository;
import com.jwtAuthentication.jwt.repository.UserRepository;
import com.jwtAuthentication.jwt.service.BookingCancellationService;
import com.jwtAuthentication.jwt.service.SeatLockService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Autowired
    private BookingCancellationService bookingCancellationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefundRepository refundRepository;

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

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Booking>>> getUserBookings(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        Long requesterId = resolveCurrentUserId(authentication);
        boolean isAdmin = isAdmin(authentication);

        if (!isAdmin && !userId.equals(requesterId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not allowed to access these bookings", null));
        }

        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Bookings fetched successfully", bookings));
    }

    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<ApiResponse<Booking>> getBookingByReference(
            @PathVariable String bookingReference,
            Authentication authentication
    ) {
        Long requesterId = resolveCurrentUserId(authentication);
        boolean isAdmin = isAdmin(authentication);

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found for reference: " + bookingReference));

        if (!isAdmin && !requesterId.equals(booking.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not allowed to access this booking", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Booking fetched successfully", booking));
    }

    @GetMapping("/user/{userId}/refunds")
    public ResponseEntity<ApiResponse<List<RefundHistoryItemDto>>> getUserRefundHistory(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        Long requesterId = resolveCurrentUserId(authentication);
        boolean isAdmin = isAdmin(authentication);

        if (!isAdmin && !userId.equals(requesterId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not allowed to access these refunds", null));
        }

        List<RefundHistoryItemDto> items = refundRepository.findUserRefundHistory(userId).stream()
                .map(this::toRefundHistoryItem)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Refund history fetched successfully", items));
    }

    @GetMapping("/{bookingId}/cancellation-preview")
    public ResponseEntity<ApiResponse<CancellationPreviewResponseDto>> getCancellationPreview(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {
        Long requesterId = resolveCurrentUserId(authentication);
        boolean isAdmin = isAdmin(authentication);

        CancellationPreviewResponseDto preview = bookingCancellationService.getCancellationPreview(
                bookingId,
                requesterId,
                isAdmin
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Cancellation preview fetched successfully", preview));
    }

    @PostMapping("/cancel-booking")
    public ResponseEntity<CancelBookingResponseDto> cancelBooking(
            @RequestBody CancelBookingRequestDto request,
            Authentication authentication
    ) {
        if (request == null || request.getBookingId() == null) {
            return ResponseEntity.badRequest().body(new CancelBookingResponseDto(
                    false,
                    "bookingId is required",
                    null,
                    null,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "FAILED",
                    null,
                    null
            ));
        }

        try {
            Long requesterId = resolveCurrentUserId(authentication);
            boolean isAdmin = isAdmin(authentication);

            CancelBookingResponseDto response = bookingCancellationService.cancelBooking(
                    request.getBookingId(),
                    requesterId,
                    isAdmin
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CancelBookingResponseDto(
                    false,
                    ex.getMessage(),
                    request.getBookingId(),
                    null,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "FAILED",
                    null,
                    null
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CancelBookingResponseDto(
                    false,
                    ex.getMessage(),
                    request.getBookingId(),
                    null,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "FAILED",
                    null,
                    null
            ));
        }
    }

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<CancelBookingResponseDto> cancelBookingByPath(
            @PathVariable("bookingId") Long bookingId,
            Authentication authentication
    ) {
        CancelBookingRequestDto request = new CancelBookingRequestDto();
        request.setBookingId(bookingId);
        return cancelBooking(request, authentication);
    }

    // Backward-compatible legacy endpoint.
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<CancelBookingResponseDto> cancelBookingLegacy(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {
        CancelBookingRequestDto request = new CancelBookingRequestDto();
        request.setBookingId(bookingId);
        return cancelBooking(request, authentication);
    }

    private Long resolveCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .map(user -> (long) user.getId())
                .orElseThrow(() -> new IllegalStateException("Unable to resolve current user"));
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private RefundHistoryItemDto toRefundHistoryItem(Refund refund) {
        Booking booking = refund.getBooking();
        String movieTitle = null;
        String theaterName = null;
        String showDate = null;
        String showTime = null;

        if (booking != null && booking.getShow() != null) {
            if (booking.getShow().getMovie() != null) {
                movieTitle = booking.getShow().getMovie().getTitle();
            }
            if (booking.getShow().getTheater() != null) {
                theaterName = booking.getShow().getTheater().getName();
            }
            showDate = booking.getShow().getShowDate() != null ? booking.getShow().getShowDate().toString() : null;
            showTime = booking.getShow().getShowTime() != null ? booking.getShow().getShowTime().toString() : null;
        }

        return new RefundHistoryItemDto(
                refund.getRefundId(),
                booking != null ? booking.getBookingId() : null,
                booking != null ? booking.getBookingReference() : null,
                movieTitle,
                theaterName,
                showDate,
                showTime,
                refund.getRefundAmount(),
                refund.getRefundPercentage(),
                refund.getRefundStatus() != null ? refund.getRefundStatus().name() : null,
                refund.getProviderReference(),
                refund.getFailureReason(),
                Objects.toString(refund.getCreatedAt(), null)
        );
    }
}
