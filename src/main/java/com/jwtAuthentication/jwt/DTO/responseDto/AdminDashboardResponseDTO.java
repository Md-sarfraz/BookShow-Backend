package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponseDTO {

    private long totalMovies;
    private long totalTheaters;
    private long totalUsers;
    private long totalEvents;
    private long totalBookings;
    private long todayBookings;
    private double totalRevenue;
    private double todayRevenue;
    private long todayShows;
    private long seatsSoldToday;
}
