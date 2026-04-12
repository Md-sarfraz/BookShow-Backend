package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventTicketDetailsResponseDto {
    private String ticketId;
    private String bookingReference;
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String eventLocation;
    private Integer ticketCount;
    private Double totalAmount;
    private String customerName;
    private String paymentStatus;
    private String qrCode;
}