package com.jwtAuthentication.jwt.DTO;

import lombok.Data;

@Data
public class PaymentVerificationRequest {
    private String razorpayOrderId;   // order_xxx
    private String razorpayPaymentId; // pay_xxx
    private String razorpaySignature; // HMAC-SHA256 signature from Razorpay
}
