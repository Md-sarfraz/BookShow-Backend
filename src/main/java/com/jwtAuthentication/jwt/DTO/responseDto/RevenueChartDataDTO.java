package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueChartDataDTO {
    private String date; // Date label (e.g., "Mar 1", "Week 1", "January")
    private double revenue; // Revenue amount
    private int bookings; // Number of bookings
}
