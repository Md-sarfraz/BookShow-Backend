package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundHistoryItemDto {
    private Long refundId;
    private Long bookingId;
    private String bookingReference;
    private String movieTitle;
    private String theaterName;
    private String showDate;
    private String showTime;
    private Double refundAmount;
    private Double refundPercentage;
    private String refundStatus;
    private String refundReference;
    private String failureReason;
    private String createdAt;
}
