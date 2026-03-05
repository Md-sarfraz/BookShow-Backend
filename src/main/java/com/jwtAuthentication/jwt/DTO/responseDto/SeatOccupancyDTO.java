package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatOccupancyDTO {
    private String movieName;
    private String theaterName;
    private String showTime;
    private String showDate;
    private Long seatsSold;
    private Integer totalSeats;
    private Double occupancyPercentage;
}
