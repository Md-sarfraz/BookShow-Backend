package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.model.NotificationType;
import com.jwtAuthentication.jwt.model.PaymentWebhookLog;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import com.jwtAuthentication.jwt.repository.PaymentWebhookLogRepository;
import com.jwtAuthentication.jwt.repository.UserRepository;
import com.jwtAuthentication.jwt.util.RazorpayWebhookSignatureVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private static final String EVENT_PAYMENT_CAPTURED = "payment.captured";
    private static final String EVENT_PAYMENT_FAILED = "payment.failed";
    private static final String EVENT_ORDER_PAID = "order.paid";

    private final BookingRepository bookingRepository;
    private final PaymentWebhookLogRepository webhookLogRepository;
    private final RazorpayWebhookSignatureVerifier signatureVerifier;
    private final SeatLockService seatLockService;
    private final NotificationService notificationService;
    private final TicketQrCodeService ticketQrCodeService;
    private final BookingEmailService bookingEmailService;
    private final UserRepository userRepository;

    @Transactional
    public Map<String, Object> processWebhook(String payload, String signatureHeader) {
        Map<String, String> extracted = extractWebhookMeta(payload);
        String eventType = extracted.getOrDefault("eventType", "UNKNOWN");
        String razorpayOrderId = extracted.get("orderId");
        String razorpayPaymentId = extracted.get("paymentId");

        PaymentWebhookLog webhookLog = new PaymentWebhookLog();
        webhookLog.setPayload(payload);
        webhookLog.setEventType(eventType);
        webhookLog.setRazorpayOrderId(razorpayOrderId);
        webhookLog.setRazorpayPaymentId(razorpayPaymentId);

        log.info(
            "Webhook received: event={} orderId={} paymentId={} signatureHeaderPresent={}",
            eventType,
            razorpayOrderId,
            razorpayPaymentId,
            signatureHeader != null && !signatureHeader.isBlank()
        );

        try {
            boolean signatureValid = signatureVerifier.isValid(payload, signatureHeader);
            webhookLog.setSignatureValid(signatureValid);

            if (!signatureValid) {
                webhookLog.setProcessingStatus("REJECTED_INVALID_SIGNATURE");
                webhookLog.setErrorMessage("Invalid Razorpay webhook signature");
                webhookLogRepository.save(webhookLog);

                log.warn(
                    "Rejected webhook due to invalid signature. event={} orderId={} paymentId={}",
                    eventType,
                    razorpayOrderId,
                    razorpayPaymentId
                );
                return Map.of(
                        "received", true,
                        "processed", false,
                        "message", "Invalid signature"
                );
            }

                JSONObject root = payload != null && !payload.isBlank() ? new JSONObject(payload) : new JSONObject();
                JSONObject payloadNode = root.optJSONObject("payload");
                JSONObject paymentEntity = payloadNode != null && payloadNode.optJSONObject("payment") != null
                    ? payloadNode.optJSONObject("payment").optJSONObject("entity")
                    : null;

                if ((razorpayOrderId == null || razorpayOrderId.isBlank())
                    && (razorpayPaymentId == null || razorpayPaymentId.isBlank())) {
                webhookLog.setProcessingStatus("IGNORED_MISSING_IDENTIFIERS");
                webhookLog.setErrorMessage("Both order_id and payment_id are missing in webhook payload");
                webhookLogRepository.save(webhookLog);

                log.warn("Webhook ignored. Missing order_id/payment_id. event={}", eventType);
                return Map.of(
                        "received", true,
                        "processed", false,
                    "message", "Missing order_id and payment_id"
                );
            }

                Optional<Booking> optionalBooking = Optional.empty();
                if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
                optionalBooking = bookingRepository.findByRazorpayOrderId(razorpayOrderId);
                }
                if (optionalBooking.isEmpty() && razorpayPaymentId != null && !razorpayPaymentId.isBlank()) {
                optionalBooking = bookingRepository.findByRazorpayPaymentId(razorpayPaymentId);
                }

            if (optionalBooking.isEmpty()) {
                webhookLog.setProcessingStatus("IGNORED_BOOKING_NOT_FOUND");
                webhookLog.setErrorMessage("No booking for order_id=" + razorpayOrderId + " or payment_id=" + razorpayPaymentId);
                webhookLogRepository.save(webhookLog);

                log.warn(
                    "Webhook ignored. Booking not found for order_id={} payment_id={} event={}",
                    razorpayOrderId,
                    razorpayPaymentId,
                    eventType
                );
                return Map.of(
                        "received", true,
                        "processed", false,
                        "message", "Booking not found"
                );
            }

            Booking booking = optionalBooking.get();

            // Late webhook should not confirm an expired booking.
            if (booking.getPaymentStatus() == Booking.PaymentStatus.EXPIRED) {
                webhookLog.setProcessingStatus("IGNORED_ALREADY_EXPIRED");
                webhookLogRepository.save(webhookLog);
                return Map.of(
                        "received", true,
                        "processed", true,
                        "message", "Booking already expired. Webhook ignored"
                );
            }

            if (booking.getPaymentStatus() == Booking.PaymentStatus.PENDING
                    && booking.getExpiresAt() != null
                    && !LocalDateTime.now().isBefore(booking.getExpiresAt())) {
                booking.setPaymentStatus(Booking.PaymentStatus.EXPIRED);
                bookingRepository.save(booking);
                if (booking.getRazorpayOrderId() != null && !booking.getRazorpayOrderId().isBlank()) {
                    seatLockService.releaseLocksForOrder(booking.getRazorpayOrderId());
                }

                webhookLog.setProcessingStatus("IGNORED_EXPIRED_AT_WEBHOOK");
                webhookLogRepository.save(webhookLog);
                return Map.of(
                        "received", true,
                        "processed", true,
                        "message", "Booking expired before webhook confirmation"
                );
            }

            boolean stateChanged = false;

            if (isSuccessEvent(eventType, paymentEntity)) {
                if (booking.getPaymentStatus() == Booking.PaymentStatus.CONFIRMED) {
                    webhookLog.setProcessingStatus("DUPLICATE_IGNORED");
                    webhookLogRepository.save(webhookLog);

                    return Map.of(
                            "received", true,
                            "processed", true,
                            "message", "Duplicate success webhook ignored"
                    );
                }

                booking.setPaymentStatus(Booking.PaymentStatus.CONFIRMED);
                booking.setConfirmedAt(LocalDateTime.now());
                if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
                    booking.setRazorpayOrderId(razorpayOrderId);
                }
                if (razorpayPaymentId != null && !razorpayPaymentId.isBlank()) {
                    booking.setRazorpayPaymentId(razorpayPaymentId);
                }

                if (booking.getQrCode() == null || booking.getQrCode().isBlank()) {
                    booking.setQrCode(ticketQrCodeService.generateBase64Png(booking));
                }

                bookingRepository.save(booking);
                if (booking.getRazorpayOrderId() != null && !booking.getRazorpayOrderId().isBlank()) {
                    seatLockService.releaseLocksForOrder(booking.getRazorpayOrderId());
                }

                String movieName = booking.getShow() != null && booking.getShow().getMovie() != null
                        ? booking.getShow().getMovie().getTitle()
                        : "Unknown movie";
                notificationService.createAndBroadcast("New booking for " + movieName, NotificationType.BOOKING);
                notificationService.createAndBroadcast("Payment successful for " + movieName, NotificationType.PAYMENT);

                sendConfirmationEmailIfPossible(booking);

                webhookLog.setProcessingStatus("CONFIRMED");
                stateChanged = true;
                log.info(
                        "Booking updated to CONFIRMED. bookingId={} bookingRef={} orderId={} paymentId={}",
                        booking.getBookingId(),
                        booking.getBookingReference(),
                        booking.getRazorpayOrderId(),
                        booking.getRazorpayPaymentId()
                );
            } else if (isFailureEvent(eventType, paymentEntity)) {
                if (booking.getPaymentStatus() == Booking.PaymentStatus.CONFIRMED) {
                    webhookLog.setProcessingStatus("IGNORED_ALREADY_CONFIRMED");
                    webhookLogRepository.save(webhookLog);

                    return Map.of(
                            "received", true,
                            "processed", true,
                            "message", "Booking already confirmed. Failure webhook ignored"
                    );
                }

                booking.setPaymentStatus(Booking.PaymentStatus.FAILED);
                if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
                    booking.setRazorpayOrderId(razorpayOrderId);
                }
                if (razorpayPaymentId != null && !razorpayPaymentId.isBlank()) {
                    booking.setRazorpayPaymentId(razorpayPaymentId);
                }

                bookingRepository.save(booking);
                if (booking.getRazorpayOrderId() != null && !booking.getRazorpayOrderId().isBlank()) {
                    seatLockService.releaseLocksForOrder(booking.getRazorpayOrderId());
                }

                String movieName = booking.getShow() != null && booking.getShow().getMovie() != null
                        ? booking.getShow().getMovie().getTitle()
                        : "Unknown movie";
                notificationService.createAndBroadcast("Payment failed for " + movieName, NotificationType.PAYMENT);

                webhookLog.setProcessingStatus("FAILED");
                stateChanged = true;
                log.info(
                        "Booking updated to FAILED. bookingId={} bookingRef={} orderId={} paymentId={}",
                        booking.getBookingId(),
                        booking.getBookingReference(),
                        booking.getRazorpayOrderId(),
                        booking.getRazorpayPaymentId()
                );
            } else {
                webhookLog.setProcessingStatus("IGNORED_EVENT");
                webhookLogRepository.save(webhookLog);

                return Map.of(
                        "received", true,
                        "processed", false,
                        "message", "Event ignored: " + eventType
                );
            }

            webhookLogRepository.save(webhookLog);
            log.info("Webhook processed. event={} orderId={} changed={}", eventType, razorpayOrderId, stateChanged);

            return Map.of(
                    "received", true,
                    "processed", true,
                    "message", "Webhook processed"
            );
        } catch (Exception ex) {
            webhookLog.setEventType(eventType);
            webhookLog.setRazorpayOrderId(razorpayOrderId);
            webhookLog.setRazorpayPaymentId(razorpayPaymentId);
            webhookLog.setSignatureValid(true);
            webhookLog.setProcessingStatus("ERROR");
            webhookLog.setErrorMessage(ex.getMessage());
            webhookLogRepository.save(webhookLog);

            log.error("Error while processing payment webhook: {}", ex.getMessage(), ex);
            return Map.of(
                    "received", true,
                    "processed", false,
                    "message", "Webhook processing failed"
            );
        }
    }

    private Map<String, String> extractWebhookMeta(String payload) {
        Map<String, String> meta = new HashMap<>();
        meta.put("eventType", "UNKNOWN");

        if (payload == null || payload.isBlank()) {
            return meta;
        }

        try {
            JSONObject root = new JSONObject(payload);
            String eventType = root.optString("event", "UNKNOWN");
            meta.put("eventType", eventType);

            JSONObject payloadNode = root.optJSONObject("payload");
            JSONObject paymentEntity = payloadNode != null && payloadNode.optJSONObject("payment") != null
                    ? payloadNode.optJSONObject("payment").optJSONObject("entity")
                    : null;
            JSONObject orderEntity = payloadNode != null && payloadNode.optJSONObject("order") != null
                    ? payloadNode.optJSONObject("order").optJSONObject("entity")
                    : null;

            String orderId = extractOrderId(paymentEntity, orderEntity);
            String paymentId = paymentEntity != null ? paymentEntity.optString("id", null) : null;

            if (orderId != null && !orderId.isBlank()) {
                meta.put("orderId", orderId);
            }
            if (paymentId != null && !paymentId.isBlank()) {
                meta.put("paymentId", paymentId);
            }
        } catch (Exception ex) {
            log.warn("Could not parse webhook payload metadata for diagnostics: {}", ex.getMessage());
        }

        return meta;
    }

    private boolean isSuccessEvent(String eventType, JSONObject paymentEntity) {
        if (EVENT_PAYMENT_CAPTURED.equalsIgnoreCase(eventType) || EVENT_ORDER_PAID.equalsIgnoreCase(eventType)) {
            return true;
        }

        String status = paymentEntity != null ? paymentEntity.optString("status", "") : "";
        return "captured".equalsIgnoreCase(status);
    }

    private boolean isFailureEvent(String eventType, JSONObject paymentEntity) {
        if (EVENT_PAYMENT_FAILED.equalsIgnoreCase(eventType)) {
            return true;
        }

        String status = paymentEntity != null ? paymentEntity.optString("status", "") : "";
        return "failed".equalsIgnoreCase(status);
    }

    private String extractOrderId(JSONObject paymentEntity, JSONObject orderEntity) {
        if (paymentEntity != null) {
            String fromPayment = paymentEntity.optString("order_id", null);
            if (fromPayment != null && !fromPayment.isBlank()) {
                return fromPayment;
            }
        }

        if (orderEntity != null) {
            String fromOrder = orderEntity.optString("id", null);
            if (fromOrder != null && !fromOrder.isBlank()) {
                return fromOrder;
            }
        }

        return null;
    }

    private void sendConfirmationEmailIfPossible(Booking booking) {
        if (booking.getUserId() == null) {
            return;
        }

        userRepository.findById(booking.getUserId().intValue())
                .map(User::getEmail)
                .ifPresent(email -> bookingEmailService.sendBookingConfirmedEmail(email, booking));
    }
}
