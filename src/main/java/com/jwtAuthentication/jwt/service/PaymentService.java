package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.*;
import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.model.NotificationType;
import com.jwtAuthentication.jwt.model.Show;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import com.jwtAuthentication.jwt.repository.ShowRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${app.payment.window-minutes:5}")
    private int paymentWindowMinutes;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatLockService seatLockService;

    @Autowired
    private NotificationService notificationService;

    /**
     * STEP 1: Create a Razorpay order server-side.
     * Amount is always calculated server-side — never trusted from frontend.
     */
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) throws RazorpayException {

        // Fetch show from DB — amount is determined HERE, not by frontend
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found: " + request.getShowId()));

        if (show.getPrice() == null) {
            throw new RuntimeException("Show price not configured for showId: " + request.getShowId());
        }

        int seatCount = request.getSeatLabels().size();
        if (seatCount == 0) {
            throw new RuntimeException("No seats selected");
        }

        // Server-side amount calculation (cannot be tampered by client)
        double baseAmount     = show.getPrice() * seatCount;
        double convenienceFee = Math.round(baseAmount * 0.10 * 100.0) / 100.0;  // 10%
        double discount       = Math.round(baseAmount * 0.18 * 100.0) / 100.0;  // 18% discount
        double totalAmount    = Math.round((baseAmount + convenienceFee - discount) * 100.0) / 100.0;
        long   amountInPaise  = (long) (totalAmount * 100); // Razorpay uses paise

        // Create Razorpay order via their API
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "BTS_" + System.currentTimeMillis());
        orderRequest.put("payment_capture", 1); // auto-capture on payment

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        // Lock seats for the payment window (5 minutes) before saving booking.
        // This prevents another user from selecting the same seats concurrently.
        seatLockService.lockSeats(
                request.getShowId(),
                request.getSeatLabels(),
                request.getUserId(),
                razorpayOrderId
        );

        // Save PENDING booking in our DB (we can reconcile later)
        Booking booking = new Booking();
        booking.setShow(show);
        booking.setUserId(request.getUserId());
        booking.setSeatLabels(String.join(",", request.getSeatLabels()));
        booking.setNumberOfSeats(seatCount);
        booking.setBaseAmount(baseAmount);
        booking.setConvenienceFee(convenienceFee);
        booking.setDiscount(discount);
        booking.setTotalAmount(totalAmount);
        booking.setRazorpayOrderId(razorpayOrderId);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(paymentWindowMinutes));

        Booking savedBooking = bookingRepository.save(booking);

        return new CreateOrderResponse(
                razorpayOrderId,
                amountInPaise,
                "INR",
                razorpayKeyId,   // public key is safe to return
                savedBooking.getBookingId(),
                savedBooking.getBookingReference()
        );
    }

    /**
     * Frontend can call this endpoint for immediate UX feedback,
     * but booking state is NOT changed here. Webhook remains source of truth.
     */
    public boolean validatePaymentSignature(PaymentVerificationRequest request) {
        try {
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = HexFormat.of().formatHex(hash);
            return computedSignature.equals(request.getRazorpaySignature());
        } catch (Exception ex) {
            return false;
        }
    }

    public Booking getPendingBookingByOrderId(String razorpayOrderId) {
        return bookingRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Booking not found for orderId: " + razorpayOrderId));
    }

    @Transactional
    public boolean markExpiredIfNeeded(Booking booking) {
        if (booking == null) {
            return false;
        }

        if (booking.getPaymentStatus() != Booking.PaymentStatus.PENDING) {
            return false;
        }

        LocalDateTime expiresAt = booking.getExpiresAt();
        if (expiresAt == null) {
            return false;
        }

        // At the exact expiry boundary, session is treated as expired.
        if (!LocalDateTime.now().isBefore(expiresAt)) {
            booking.setPaymentStatus(Booking.PaymentStatus.EXPIRED);
            bookingRepository.save(booking);
            if (booking.getRazorpayOrderId() != null && !booking.getRazorpayOrderId().isBlank()) {
                seatLockService.releaseLocksForOrder(booking.getRazorpayOrderId());
            }
            return true;
        }

        return false;
    }

    public void assertNotExpired(Booking booking) {
        if (booking == null) {
            throw new RuntimeException("Booking not found");
        }

        if (booking.getPaymentStatus() == Booking.PaymentStatus.EXPIRED) {
            throw new RuntimeException("Session expired");
        }

        if (markExpiredIfNeeded(booking)) {
            throw new RuntimeException("Session expired");
        }
    }

    /**
     * Fallback confirmation path:
     * If webhook is delayed but payment signature is verified, confirm booking once
     * while preserving idempotency and expiry checks.
     */
    @Transactional
    public Booking confirmBookingAfterSignatureVerified(String razorpayOrderId, String razorpayPaymentId) {
        Booking booking = bookingRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Booking not found for orderId: " + razorpayOrderId));

        if (booking.getPaymentStatus() == Booking.PaymentStatus.CONFIRMED) {
            return booking;
        }

        if (booking.getPaymentStatus() == Booking.PaymentStatus.EXPIRED) {
            throw new RuntimeException("Session expired");
        }

        if (booking.getPaymentStatus() == Booking.PaymentStatus.FAILED
                || booking.getPaymentStatus() == Booking.PaymentStatus.CANCELLED) {
            throw new RuntimeException("Booking is not eligible for confirmation");
        }

        assertNotExpired(booking);

        booking.setPaymentStatus(Booking.PaymentStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        if (razorpayPaymentId != null && !razorpayPaymentId.isBlank()) {
            booking.setRazorpayPaymentId(razorpayPaymentId);
        }

        Booking saved = bookingRepository.save(booking);
        if (saved.getRazorpayOrderId() != null && !saved.getRazorpayOrderId().isBlank()) {
            seatLockService.releaseLocksForOrder(saved.getRazorpayOrderId());
        }

        String movieName = saved.getShow() != null && saved.getShow().getMovie() != null
                ? saved.getShow().getMovie().getTitle()
                : "Unknown movie";
        notificationService.createAndBroadcast("New booking for " + movieName, NotificationType.BOOKING);
        notificationService.createAndBroadcast("Payment successful for " + movieName, NotificationType.PAYMENT);

        return saved;
    }
}
