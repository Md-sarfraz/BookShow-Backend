package com.jwtAuthentication.jwt.controller;

import com.jwtAuthentication.jwt.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping({"/api/webhook/payment", "/webhook/payment"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> handlePaymentWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String razorpaySignature,
            HttpServletRequest request
    ) {
        log.info(
                "Webhook ingress: path={} method={} from={} signatureHeaderPresent={} payloadSize={} bytes",
                request.getRequestURI(),
                request.getMethod(),
                request.getRemoteAddr(),
                razorpaySignature != null && !razorpaySignature.isBlank(),
                payload != null ? payload.length() : 0
        );

        Map<String, Object> response = paymentWebhookService.processWebhook(payload, razorpaySignature);

        // Razorpay expects 2xx response; we always return HTTP 200 and handle retries via idempotent processing.
        return ResponseEntity.ok(response);
    }
}
