package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelBookingResponseDto {
    private boolean success;
    private String message;
    private Long bookingId;
    private String bookingStatus;
    private Double refundableAmount;
    private Double refundAmount;
    private Double convenienceFeeDeducted;
    private Double refundPercentage;
    private String refundStatus;
    private String refundReference;
    private String failureReason;
}
