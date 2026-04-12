package com.jwtAuthentication.jwt.DTO.requestDto;

import lombok.Data;

@Data
public class EventLockTicketsRequestDto {
    private Integer eventId;
    private Long userId;
    private Integer ticketCount;
}
