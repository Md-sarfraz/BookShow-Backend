package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.responseDto.AdminDashboardResponseDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.RevenueChartDataDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.MoviePerformanceDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.TheaterPerformanceDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.SeatOccupancyDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.ReportSummaryDTO;
import com.jwtAuthentication.jwt.model.Activity;
import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.service.ActivityService;
import com.jwtAuthentication.jwt.service.AdminService;
import com.jwtAuthentication.jwt.service.CloudinaryImageService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

        private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;
    private final ActivityService activityService;

    @Autowired
    private CloudinaryImageService cloudinaryImageService;

    public AdminController(AdminService adminService, ActivityService activityService) {
        this.adminService = adminService;
        this.activityService = activityService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponseDTO>> getDashboardCounts() {

        AdminDashboardResponseDTO data =
                adminService.getDashboardCounts();

        ApiResponse<AdminDashboardResponseDTO> response =
                new ApiResponse<>(
                        true,
                        "Dashboard counts fetched successfully",
                        data
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings() {
        List<Booking> bookings = adminService.getAllBookings();
        
        ApiResponse<List<Booking>> response =
                new ApiResponse<>(
                        true,
                        "Bookings fetched successfully",
                        bookings
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/bookings/{bookingId}/status")
    public ResponseEntity<ApiResponse<Booking>> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestParam String status) {
        
        Booking updatedBooking = adminService.updateBookingStatus(bookingId, status);
        
        ApiResponse<Booking> response =
                new ApiResponse<>(
                        true,
                        "Booking status updated successfully",
                        updatedBooking
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<ApiResponse<List<RevenueChartDataDTO>>> getRevenueChart(
            @RequestParam(defaultValue = "weekly") String period) {
        
        List<RevenueChartDataDTO> chartData = adminService.getRevenueChartData(period);
        
        ApiResponse<List<RevenueChartDataDTO>> response =
                new ApiResponse<>(
                        true,
                        "Revenue chart data fetched successfully",
                        chartData
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<List<Activity>>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Activity> activities = activityService.getRecentActivities(limit);
        
        ApiResponse<List<Activity>> response =
                new ApiResponse<>(
                        true,
                        "Activities fetched successfully",
                        activities
                );

        return ResponseEntity.ok(response);
    }

    // Report Endpoints
    @GetMapping("/reports/summary")
    public ResponseEntity<ApiResponse<ReportSummaryDTO>> getReportSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDateTime start = startDate != null ? 
            LocalDate.parse(startDate).atStartOfDay() : 
            LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? 
            LocalDate.parse(endDate).atTime(LocalTime.MAX) : 
            LocalDateTime.now();
        
        ReportSummaryDTO summary = adminService.getReportSummary(start, end);
        
        ApiResponse<ReportSummaryDTO> response =
                new ApiResponse<>(
                        true,
                        "Report summary fetched successfully",
                        summary
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/movie-performance")
    public ResponseEntity<ApiResponse<List<MoviePerformanceDTO>>> getMoviePerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDateTime start = startDate != null ? 
            LocalDate.parse(startDate).atStartOfDay() : 
            LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? 
            LocalDate.parse(endDate).atTime(LocalTime.MAX) : 
            LocalDateTime.now();
        
        List<MoviePerformanceDTO> performance = adminService.getMoviePerformance(start, end);
        
        ApiResponse<List<MoviePerformanceDTO>> response =
                new ApiResponse<>(
                        true,
                        "Movie performance fetched successfully",
                        performance
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/theater-performance")
    public ResponseEntity<ApiResponse<List<TheaterPerformanceDTO>>> getTheaterPerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDateTime start = startDate != null ? 
            LocalDate.parse(startDate).atStartOfDay() : 
            LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? 
            LocalDate.parse(endDate).atTime(LocalTime.MAX) : 
            LocalDateTime.now();
        
        List<TheaterPerformanceDTO> performance = adminService.getTheaterPerformance(start, end);
        
        ApiResponse<List<TheaterPerformanceDTO>> response =
                new ApiResponse<>(
                        true,
                        "Theater performance fetched successfully",
                        performance
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/seat-occupancy")
    public ResponseEntity<ApiResponse<List<SeatOccupancyDTO>>> getSeatOccupancy(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDateTime start = startDate != null ? 
            LocalDate.parse(startDate).atStartOfDay() : 
            LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? 
            LocalDate.parse(endDate).atTime(LocalTime.MAX) : 
            LocalDateTime.now();
        
        List<SeatOccupancyDTO> occupancy = adminService.getSeatOccupancy(start, end);
        
        ApiResponse<List<SeatOccupancyDTO>> response =
                new ApiResponse<>(
                        true,
                        "Seat occupancy fetched successfully",
                        occupancy
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/booking-trends")
    public ResponseEntity<ApiResponse<List<RevenueChartDataDTO>>> getBookingTrends(
            @RequestParam(defaultValue = "weekly") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        // Reuse the revenue chart data method since it also has booking counts
        List<RevenueChartDataDTO> chartData = adminService.getRevenueChartData(period);
        
        ApiResponse<List<RevenueChartDataDTO>> response =
                new ApiResponse<>(
                        true,
                        "Booking trends fetched successfully",
                        chartData
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<User>> getAdminProfile(@PathVariable int userId) {
        User user = adminService.getUserById(userId);
        ApiResponse<User> response = new ApiResponse<>(
                true,
                "Admin profile fetched successfully",
                user
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<User>> updateAdminProfile(
            @PathVariable int userId,
            @RequestBody User updatedUser) {
        User user = adminService.updateUserProfile(userId, updatedUser);
        ApiResponse<User> response = new ApiResponse<>(
                true,
                "Profile updated successfully",
                user
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile/{userId}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable int userId,
            @RequestBody java.util.Map<String, String> passwordData) {
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

                logger.info("Password update requested for userId={}", userId);

                if (currentPassword == null || currentPassword.trim().isEmpty()) {
                        throw new IllegalArgumentException("Current password is required");
                }
                if (newPassword == null || newPassword.trim().isEmpty()) {
                        throw new IllegalArgumentException("New password is required");
                }

        adminService.updatePassword(userId, currentPassword, newPassword);

                logger.info("Password updated successfully for userId={}", userId);

        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "Password updated successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile/{userId}/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @PathVariable int userId,
            @RequestParam("file") MultipartFile file) {
        try {
            java.util.Map uploadResult = cloudinaryImageService.upload(file, userId);
            String imageUrl = uploadResult.get("url").toString();
            User user = adminService.updateUserAvatar(userId, imageUrl);
            ApiResponse<String> response = new ApiResponse<>(
                    true,
                    "Avatar uploaded successfully",
                    imageUrl
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                    false,
                    "Failed to upload avatar: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}
