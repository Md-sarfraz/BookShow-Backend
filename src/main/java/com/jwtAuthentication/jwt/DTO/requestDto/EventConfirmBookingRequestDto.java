package com.jwtAuthentication.jwt.DTO.requestDto;

import lombok.Data;

@Data
public class EventConfirmBookingRequestDto {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String name;
    private String email;
    private String phone;
}
