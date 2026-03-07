package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.*;
import com.jwtAuthentication.jwt.model.Booking;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

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
     * STEP 2: Verify Razorpay payment signature server-side (HMAC-SHA256).
     * This is the security-critical step — only backend has the secret key.
     */
    @Transactional
    public BookingConfirmationResponse verifyAndConfirmPayment(PaymentVerificationRequest request)
            throws NoSuchAlgorithmException, InvalidKeyException {

        // --- SIGNATURE VERIFICATION ---
        // Razorpay signs: razorpay_order_id + "|" + razorpay_payment_id
        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String computedSignature = HexFormat.of().formatHex(hash);

        if (!computedSignature.equals(request.getRazorpaySignature())) {
            // Signature mismatch — possible tampered/fake payment, reject it
            throw new SecurityException("Payment signature verification failed. Possible fraud attempt.");
        }

        // --- SIGNATURE VALID — Update booking record ---
        Booking booking = bookingRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found for orderId: " + request.getRazorpayOrderId()));

        booking.setRazorpayPaymentId(request.getRazorpayPaymentId());
        booking.setPaymentStatus(Booking.PaymentStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());

        Booking confirmedBooking = bookingRepository.save(booking);

        // Build response
        Show show = confirmedBooking.getShow();

        return new BookingConfirmationResponse(
                confirmedBooking.getBookingId(),
                confirmedBooking.getBookingReference(),
                confirmedBooking.getRazorpayPaymentId(),
                confirmedBooking.getTotalAmount(),
                confirmedBooking.getBaseAmount(),
                confirmedBooking.getConvenienceFee(),
                confirmedBooking.getDiscount() != null ? confirmedBooking.getDiscount() : 0.0,
                confirmedBooking.getNumberOfSeats(),
                confirmedBooking.getSeatLabels(),
                show.getMovie() != null ? show.getMovie().getTitle() : "",
                show.getTheater() != null ? show.getTheater().getName() : "",
                show.getTheater() != null && show.getTheater().getCity() != null ? show.getTheater().getCity().getName() : "",
                show.getShowDate() != null ? show.getShowDate().toString() : "",
                show.getShowTime() != null ? show.getShowTime().toString() : "",
                confirmedBooking.getPaymentStatus().name()
        );
    }

    /**
     * Mark a PENDING booking as FAILED when Razorpay reports payment failure.
     * Only transitions from PENDING to avoid overwriting a later CONFIRMED state
     * in case of out-of-order callbacks.
     */
    @Transactional
    public void handlePaymentFailed(String razorpayOrderId) {
        bookingRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(booking -> {
            if (booking.getPaymentStatus() == Booking.PaymentStatus.PENDING) {
                booking.setPaymentStatus(Booking.PaymentStatus.FAILED);
                bookingRepository.save(booking);
            }
        });
    }
}
