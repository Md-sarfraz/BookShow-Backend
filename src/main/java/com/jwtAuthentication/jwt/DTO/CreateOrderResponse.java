package com.jwtAuthentication.jwt.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse {
    private String razorpayOrderId;   // order_xxx from Razorpay
    private Long   amountInPaise;     // e.g. 46000 for ₹460.00
    private String currency;          // "INR"
    private String keyId;             // public key — safe to send to frontend
    private Long   bookingId;         // our DB booking ID (PENDING status)
    private String bookingReference;  // BTS... reference
}
