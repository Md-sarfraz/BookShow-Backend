package com.jwtAuthentication.jwt.DTO.requestDto;

import lombok.Data;

@Data
public class EventBookingRequestDto {
    private Integer eventId;
    private Long userId;
    private Integer numberOfTickets;
}
