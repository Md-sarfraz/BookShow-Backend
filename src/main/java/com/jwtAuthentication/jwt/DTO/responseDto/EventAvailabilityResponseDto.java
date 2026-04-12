package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventAvailabilityResponseDto {
    private boolean available;
    private long remainingTickets;
}
