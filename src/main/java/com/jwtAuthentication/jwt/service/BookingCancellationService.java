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

    @Value("${app.refund.partial-percentage:70}")
    private double partialRefundPercentage;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private RefundProcessor refundProcessor;

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
                calc.showDateTime.toString()
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
        } else {
            RefundResult result = refundProcessor.initiateRefund(booking, calc.refundAmount);
            if (result.successful()) {
                refund.setRefundStatus(Refund.RefundStatus.SUCCESS);
                refund.setProviderReference(result.providerReference());
            } else {
                refund.setRefundStatus(Refund.RefundStatus.FAILED);
                refund.setFailureReason(result.errorMessage());
            }
        }
        refundRepository.save(refund);

        publishSeatRelease(booking);

        String message = refund.getRefundStatus() == Refund.RefundStatus.FAILED
                ? "Booking cancelled. Refund initiation failed. Please contact support."
                : "Booking cancelled successfully";

        return new CancelBookingResponseDto(
                true,
                message,
                booking.getBookingId(),
                booking.getPaymentStatus().name(),
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
        if (booking.getPaymentStatus() == Booking.PaymentStatus.CANCELLED) {
            return Calculation.blocked("Booking is already cancelled", booking);
        }

        if (booking.getPaymentStatus() != Booking.PaymentStatus.CONFIRMED) {
            return Calculation.blocked("Only confirmed bookings can be cancelled", booking);
        }

        Show show = booking.getShow();
        if (show == null || show.getShowDate() == null || show.getShowTime() == null) {
            throw new IllegalStateException("Show schedule is missing for this booking");
        }

        LocalDateTime showDateTime = LocalDateTime.of(show.getShowDate(), show.getShowTime());

        if (!now.isBefore(showDateTime)) {
            return Calculation.blocked("Cancellation is not allowed after showtime", booking, showDateTime);
        }

        long minutesToShow = Duration.between(now, showDateTime).toMinutes();
        long cutoffMinutes = Math.max(0, cancellationCutoffHours * 60);

        if (minutesToShow < cutoffMinutes) {
            return Calculation.blocked(
                    "Cancellation is allowed only up to " + cancellationCutoffHours + " hours before showtime",
                    booking,
                    showDateTime
            );
        }

        double total = nonNull(booking.getTotalAmount());
        double convenienceFee = Math.max(0, nonNull(booking.getConvenienceFee()));
        double refundableBase = round2(Math.max(0, total - convenienceFee));

        double refundPercentage;
        if (minutesToShow > 24 * 60) {
            refundPercentage = 100.0;
        } else if (minutesToShow >= 2 * 60) {
            refundPercentage = partialRefundPercentage;
        } else {
            refundPercentage = 0.0;
        }

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
                showDateTime
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
            LocalDateTime showDateTime
    ) {
        private static Calculation blocked(String message, Booking booking) {
            return blocked(message, booking, booking.getShow() != null && booking.getShow().getShowDate() != null && booking.getShow().getShowTime() != null
                    ? LocalDateTime.of(booking.getShow().getShowDate(), booking.getShow().getShowTime())
                    : LocalDateTime.now());
        }

        private static Calculation blocked(String message, Booking booking, LocalDateTime showDateTime) {
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
                    showDateTime
            );
        }
    }
}
