package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MockRefundProcessor implements RefundProcessor {

    @Value("${app.refund.mock-failure-rate:0.0}")
    private double mockFailureRate;

    @Override
    public RefundResult initiateRefund(Booking booking, double refundAmount) {
        if (refundAmount <= 0) {
            return new RefundResult(true, "NO_REFUND", null);
        }

        double random = ThreadLocalRandom.current().nextDouble();
        if (random < mockFailureRate) {
            return new RefundResult(false, null, "Mock payment gateway failure while initiating refund");
        }

        String reference = "RFND_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return new RefundResult(true, reference, null);
    }
}
