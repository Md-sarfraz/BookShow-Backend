package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventPaymentOrderResponseDto {
    private String razorpayOrderId;
    private Long amountInPaise;
    private String currency;
    private String keyId;
    private Long bookingId;
    private String bookingReference;
}
