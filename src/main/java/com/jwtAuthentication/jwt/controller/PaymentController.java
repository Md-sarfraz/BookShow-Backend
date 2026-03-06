package com.jwtAuthentication.jwt.controller;

import com.jwtAuthentication.jwt.DTO.*;
import com.jwtAuthentication.jwt.service.PaymentService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * STEP 1 — Frontend calls this to create a server-side Razorpay order.
     * Amount is ALWAYS computed on server from DB price — client cannot tamper it.
     *
     * POST /api/v1/payment/create-order
     * Body: { "showId": 1, "seatLabels": ["A1", "A2"], "userId": 5 }
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            if (request.getShowId() == null || request.getSeatLabels() == null
                    || request.getSeatLabels().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "showId and seatLabels are required", null));
            }

            CreateOrderResponse orderResponse = paymentService.createOrder(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Order created successfully", orderResponse));

        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiResponse<>(false, "Failed to create payment order: " + e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * STEP 2 — Frontend calls this after Razorpay returns payment success.
     * Backend verifies HMAC-SHA256 signature using the SECRET key (never exposes it).
     * Only after signature passes does it confirm the booking in DB.
     *
     * POST /api/v1/payment/verify
     * Body: { "razorpayOrderId": "order_xxx", "razorpayPaymentId": "pay_xxx", "razorpaySignature": "..." }
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<BookingConfirmationResponse>> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        try {
            if (request.getRazorpayOrderId() == null || request.getRazorpayPaymentId() == null
                    || request.getRazorpaySignature() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "razorpayOrderId, razorpayPaymentId and razorpaySignature are required", null));
            }

            BookingConfirmationResponse confirmation = paymentService.verifyAndConfirmPayment(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment verified and booking confirmed", confirmation));

        } catch (SecurityException e) {
            // Signature mismatch — log and reject
            System.err.println("🚨 SECURITY ALERT: Payment signature verification failed for order: "
                    + request.getRazorpayOrderId());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(new ApiResponse<>(false, "Payment verification failed. Please contact support.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Payment verification error: " + e.getMessage(), null));
        }
    }
}
