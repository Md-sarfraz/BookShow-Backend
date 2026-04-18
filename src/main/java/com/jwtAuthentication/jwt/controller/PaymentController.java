package com.jwtAuthentication.jwt.controller;

import com.jwtAuthentication.jwt.DTO.*;
import com.jwtAuthentication.jwt.repository.UserRepository;
import com.jwtAuthentication.jwt.service.PaymentService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * STEP 1 — Frontend calls this to create a server-side Razorpay order.
     * Amount is ALWAYS computed on server from DB price — client cannot tamper it.
     *
     * POST /api/v1/payment/create-order
     * Body: { "showId": 1, "seatLabels": ["A1", "A2"], "userId": 5 }
     */
    @PostMapping("/create-order")
        public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @RequestBody CreateOrderRequest request,
            Authentication authentication
        ) {
        try {
            if (request.getShowId() == null || request.getSeatLabels() == null
                    || request.getSeatLabels().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "showId and seatLabels are required", null));
            }

            Long userId = resolveCurrentUserId(authentication);
            request.setUserId(userId);

            CreateOrderResponse orderResponse = paymentService.createOrder(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Order created successfully", orderResponse));

        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiResponse<>(false, "Failed to create payment order: " + e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Frontend verification endpoint for UX only.
     * Booking state is NOT changed here; webhook is source of truth.
     */
    @PostMapping("/verify")
        public ResponseEntity<ApiResponse<String>> verifyPayment(
            @RequestBody PaymentVerificationRequest request,
            Authentication authentication
        ) {
        try {
            if (request.getRazorpayOrderId() == null || request.getRazorpayPaymentId() == null
                    || request.getRazorpaySignature() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "razorpayOrderId, razorpayPaymentId and razorpaySignature are required", null));
            }

            Long userId = resolveCurrentUserId(authentication);
            var booking = paymentService.getPendingBookingByOrderId(request.getRazorpayOrderId());
            if (booking.getUserId() == null || !booking.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "You are not allowed to verify this booking", null));
            }

            paymentService.assertNotExpired(booking);

            boolean signatureValid = paymentService.validatePaymentSignature(request);
            if (!signatureValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Invalid payment signature", null));
            }

                // Fallback for delayed/missed webhook: confirm booking once after successful signature verification.
                paymentService.confirmBookingAfterSignatureVerified(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId()
                );

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Payment signature validated and booking confirmation initiated.",
                    "CONFIRMATION_INITIATED"
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Payment verification error: " + e.getMessage(), null));
        }
    }

    /**
     * Kept for compatibility with older frontend.
     * Booking is not updated here; webhook endpoint is authoritative.
     */
    @PostMapping("/failed")
    public ResponseEntity<ApiResponse<String>> handlePaymentFailed(
            @RequestParam String razorpayOrderId,
            Authentication authentication
    ) {
        try {
            if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "razorpayOrderId is required", null));
            }

            resolveCurrentUserId(authentication);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Failure event acknowledged. Waiting for webhook to update booking status.",
                    "WEBHOOK_PENDING"
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating booking status: " + e.getMessage(), null));
        }
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
}
