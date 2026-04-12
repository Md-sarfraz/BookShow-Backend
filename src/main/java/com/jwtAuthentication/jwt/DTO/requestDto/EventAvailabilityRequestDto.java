package com.jwtAuthentication.jwt.DTO.requestDto;

import lombok.Data;

@Data
public class EventAvailabilityRequestDto {
    private Integer eventId;
    private Integer ticketCount;
}
