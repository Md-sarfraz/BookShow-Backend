package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportSummaryDTO {
    private Double totalRevenue;
    private Long totalBookings;
    private Long totalTicketsSold;
    private Double averageBookingValue;
}
