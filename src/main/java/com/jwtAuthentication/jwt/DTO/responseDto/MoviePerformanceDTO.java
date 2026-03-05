package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoviePerformanceDTO {
    private String movieName;
    private Integer movieId;
    private Long totalTicketsSold;
    private Double totalRevenue;
}
