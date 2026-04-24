package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.responseDto.CancelBookingResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.CancellationPreviewResponseDto;
import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.model.Refund;
import com.jwtAuthentication.jwt.model.Show;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import com.jwtAuthentication.jwt.repository.RefundRepository;
import com.jwtAuthentication.jwt.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingCancellationService {

    @Value("${app.cancellation.cutoff-hours:2}")
    private long cancellationCutoffHours;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private BookingRefundAsyncService bookingRefundAsyncService;

    @Autowired
    private SeatLockService seatLockService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public CancellationPreviewResponseDto getCancellationPreview(Long bookingId, Long requesterUserId, boolean admin) {
        Booking booking = getBookingForRequester(bookingId, requesterUserId, admin);
        Calculation calc = calculateRefund(booking, LocalDateTime.now());

        return new CancellationPreviewResponseDto(
                calc.cancellationAllowed,
                calc.message,
                booking.getBookingId(),
                calc.refundableAmount,
                calc.refundAmount,
                calc.convenienceFeeDeducted,
                calc.refundPercentage,
                calc.refundStatus,
                calc.bookingTime.toString(),
                calc.cancellationDeadline.toString(),
                cancellationCutoffHours
        );
    }

    @Transactional
    public CancelBookingResponseDto cancelBooking(Long bookingId, Long requesterUserId, boolean admin) {
        Booking booking = getBookingForRequester(bookingId, requesterUserId, admin);
        Calculation calc = calculateRefund(booking, LocalDateTime.now());

        if (!calc.cancellationAllowed) {
            throw new IllegalStateException(calc.message);
        }

        if (booking.getPaymentStatus() == Booking.PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        booking.setPaymentStatus(Booking.PaymentStatus.CANCELLED);
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Return sold seats to inventory immediately.
        Show show = booking.getShow();
        if (show != null && show.getAvailableSeats() != null && booking.getNumberOfSeats() != null) {
            int totalSeats = show.getTotalSeats() != null ? show.getTotalSeats() : Integer.MAX_VALUE;
            int updatedAvailable = Math.min(totalSeats, show.getAvailableSeats() + booking.getNumberOfSeats());
            show.setAvailableSeats(updatedAvailable);
            showRepository.save(show);
        }

        if (booking.getRazorpayOrderId() != null) {
            seatLockService.releaseLocksForOrder(booking.getRazorpayOrderId());
        }

        Refund refund = new Refund();
        refund.setBooking(booking);
        refund.setPaidAmount(nonNull(booking.getTotalAmount()));
        refund.setRefundableAmount(calc.refundableAmount);
        refund.setRefundAmount(calc.refundAmount);
        refund.setConvenienceFeeDeducted(calc.convenienceFeeDeducted);
        refund.setRefundPercentage(calc.refundPercentage);
        refund.setPaymentProvider("MOCK_GATEWAY");
        refund.setRefundStatus(Refund.RefundStatus.PENDING);
        refundRepository.save(refund);

        if (calc.refundAmount <= 0) {
            refund.setRefundStatus(Refund.RefundStatus.SUCCESS);
            refund.setProviderReference("NO_REFUND");
            refund.setFailureReason("No refund as per cancellation policy");
            refundRepository.save(refund);
        } else {
            refundRepository.save(refund);
            bookingRefundAsyncService.processRefundAsync(refund.getRefundId(), booking.getBookingId(), calc.refundAmount);
        }

        publishSeatRelease(booking);

        String message = calc.refundAmount <= 0
                ? "Booking cancelled successfully"
                : "Booking cancelled successfully. Refund initiated.";

        return new CancelBookingResponseDto(
                true,
                message,
                booking.getBookingId(),
            booking.getStatus().name(),
                refund.getRefundableAmount(),
                refund.getRefundAmount(),
                refund.getConvenienceFeeDeducted(),
                refund.getRefundPercentage(),
                refund.getRefundStatus().name(),
                refund.getProviderReference(),
                refund.getFailureReason()
        );
    }

    private Booking getBookingForRequester(Long bookingId, Long requesterUserId, boolean admin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found for id: " + bookingId));

        if (!admin) {
            Long ownerId = booking.getUserId();
            if (ownerId == null || requesterUserId == null || !ownerId.equals(requesterUserId)) {
                throw new IllegalStateException("You are not allowed to access this booking");
            }
        }

        return booking;
    }

    private Calculation calculateRefund(Booking booking, LocalDateTime now) {
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED
                || booking.getPaymentStatus() == Booking.PaymentStatus.CANCELLED) {
            return Calculation.blocked("Booking is already cancelled", booking);
        }

        if (booking.getPaymentStatus() != Booking.PaymentStatus.CONFIRMED) {
            return Calculation.blocked("Only confirmed bookings can be cancelled", booking);
        }

        LocalDateTime bookingTime = booking.getCreatedAt();
        if (bookingTime == null) {
            throw new IllegalStateException("Booking time is missing for this booking");
        }

        long windowHours = Math.max(0, cancellationCutoffHours);
        Duration elapsedSinceBooking = Duration.between(bookingTime, now);
        if (elapsedSinceBooking.isNegative()) {
            elapsedSinceBooking = Duration.ZERO;
        }
        Duration cancellationWindow = Duration.ofHours(windowHours);
        LocalDateTime cancellationDeadline = bookingTime.plusHours(windowHours);

        if (elapsedSinceBooking.compareTo(cancellationWindow) > 0) {
            return Calculation.blocked("Cancellation window expired", booking, bookingTime, cancellationDeadline);
        }

        double total = nonNull(booking.getTotalAmount());
        double convenienceFee = Math.max(0, nonNull(booking.getConvenienceFee()));
        double refundableBase = round2(Math.max(0, total - convenienceFee));

        double refundPercentage = refundableBase <= 0 ? 0.0 : 100.0;

        double refundAmount = round2(refundableBase * (refundPercentage / 100.0));
        String refundStatus = refundAmount <= 0 ? Refund.RefundStatus.SUCCESS.name() : Refund.RefundStatus.PENDING.name();

        return new Calculation(
                true,
                "Cancellation allowed",
                refundableBase,
                refundAmount,
                convenienceFee,
                round2(refundPercentage),
                refundStatus,
                bookingTime,
                cancellationDeadline
        );
    }

    private void publishSeatRelease(Booking booking) {
        if (booking.getShow() == null || booking.getShow().getShowId() == null) {
            return;
        }

        List<String> releasedSeats = booking.getSeatLabels() == null
                ? List.of()
                : Arrays.stream(booking.getSeatLabels().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "SEAT_RELEASED");
        payload.put("showId", booking.getShow().getShowId());
        payload.put("bookingId", booking.getBookingId());
        payload.put("releasedSeats", releasedSeats);
        payload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/seats/" + booking.getShow().getShowId(), payload);
    }

    private double nonNull(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record Calculation(
            boolean cancellationAllowed,
            String message,
            double refundableAmount,
            double refundAmount,
            double convenienceFeeDeducted,
            double refundPercentage,
            String refundStatus,
            LocalDateTime bookingTime,
            LocalDateTime cancellationDeadline
    ) {
        private static Calculation blocked(String message, Booking booking) {
            LocalDateTime bookingTime = booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.now();
            return blocked(message, booking, bookingTime, bookingTime);
        }

        private static Calculation blocked(
            String message,
            Booking booking,
            LocalDateTime bookingTime,
            LocalDateTime cancellationDeadline
        ) {
            double total = booking.getTotalAmount() == null ? 0.0 : booking.getTotalAmount();
            double convenience = booking.getConvenienceFee() == null ? 0.0 : Math.max(0, booking.getConvenienceFee());
            double refundableBase = Math.max(0, total - convenience);
            return new Calculation(
                    false,
                    message,
                    Math.round(refundableBase * 100.0) / 100.0,
                    0.0,
                    convenience,
                    0.0,
                    Refund.RefundStatus.SUCCESS.name(),
                    bookingTime,
                    cancellationDeadline
            );
        }
    }
}
