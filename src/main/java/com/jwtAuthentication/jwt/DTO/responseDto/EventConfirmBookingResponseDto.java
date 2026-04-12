package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventConfirmBookingResponseDto {
    private String ticketId;
    private String qrCode;
    private EventBookingResponseDto bookingDetails;
}
