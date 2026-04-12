package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EventLockTicketsResponseDto {
    private Long bookingId;
    private String bookingReference;
    private LocalDateTime expiresAt;
    private Double unitPrice;
    private Double totalAmount;
}
