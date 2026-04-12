package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventBookingResponseDto {
    private Long bookingId;
    private String bookingReference;
    private Integer eventId;
    private String eventTitle;
    private String eventDate;
    private String eventTime;
    private String location;
    private Integer numberOfTickets;
    private Double unitPrice;
    private Double totalAmount;
    private String status;
    private String imageUrl;
    private LocalDateTime createdAt;
}
