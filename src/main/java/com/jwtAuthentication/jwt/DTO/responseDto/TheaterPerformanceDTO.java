package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheaterPerformanceDTO {
    private String theaterName;
    private Integer theaterId;
    private Long totalBookings;
    private Double totalRevenue;
}
