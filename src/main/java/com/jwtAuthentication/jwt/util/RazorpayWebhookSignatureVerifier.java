package com.jwtAuthentication.jwt.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Slf4j
@Component
public class RazorpayWebhookSignatureVerifier {

    private final String webhookSecret;

    public RazorpayWebhookSignatureVerifier(@Value("${razorpay.webhook.secret:}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
        if (this.webhookSecret == null || this.webhookSecret.isBlank()) {
            log.warn("RAZORPAY_WEBHOOK_SECRET is not configured. All webhook signature validations will fail.");
        }
    }

    public boolean isValid(String payload, String providedSignature) {
        if (payload == null || payload.isBlank()) {
            return false;
        }
        if (providedSignature == null || providedSignature.isBlank()) {
            return false;
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Cannot verify Razorpay webhook signature because webhook secret is missing.");
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = HexFormat.of().formatHex(hash);
            return computedSignature.equalsIgnoreCase(providedSignature.trim());
        } catch (Exception ex) {
            return false;
        }
    }
}
