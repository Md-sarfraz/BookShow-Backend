package com.jwtAuthentication.jwt.controller;

import com.jwtAuthentication.jwt.model.Show;
import com.jwtAuthentication.jwt.service.ShowService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/shows")
@CrossOrigin(origins = "http://localhost:5174")
public class ShowController {
    
    @Autowired
    private ShowService showService;
    
    // Create a new show (Admin only)
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Show>> createShow(@RequestBody Show show) {
        try {
            Show createdShow = showService.createShow(show);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Show created successfully", createdShow));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error creating show: " + e.getMessage(), null));
        }
    }
    
    // Create show by movie and theater IDs
    @PostMapping("/create/{movieId}/{theaterId}")
    public ResponseEntity<ApiResponse<Show>> createShowByIds(
            @PathVariable int movieId,
            @PathVariable int theaterId,
            @RequestBody Show show) {
        try {
            Show createdShow = showService.createShow(movieId, theaterId, show);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Show created successfully", createdShow));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error creating show: " + e.getMessage(), null));
        }
    }
    
    // Get all shows
    @GetMapping
    public ResponseEntity<ApiResponse<List<Show>>> getAllShows() {
        List<Show> shows = showService.getAllShows();
        return ResponseEntity.ok(new ApiResponse<>(true, "Shows fetched successfully", shows));
    }
    
    // Get show by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Show>> getShowById(@PathVariable Long id) {
        Optional<Show> show = showService.getShowById(id);
        if (show.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Show fetched successfully", show.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Show not found", null));
    }
    
    // Get shows by movie (with optional city and date filters)
    @GetMapping("/by-movie/{movieId}")
    public ResponseEntity<ApiResponse<List<Show>>> getShowsByMovie(
            @PathVariable int movieId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        System.out.println("========================================");
        System.out.println("📋 GET /api/v1/shows/by-movie/" + movieId);
        System.out.println("   city: " + city + ", date: " + date);
        System.out.println("========================================");
        
        List<Show> shows;
        
        if (city != null && date != null) {
            shows = showService.getShowsByMovieAndCityAndDate(movieId, city, date);
        } else if (city != null) {
            shows = showService.getShowsByMovieAndCity(movieId, city);
        } else if (date != null) {
            shows = showService.getShowsByMovieAndDate(movieId, date);
        } else {
            shows = showService.getShowsByMovie(movieId);
        }
        
        System.out.println("📤 Returning " + shows.size() + " shows to frontend");
        return ResponseEntity.ok(new ApiResponse<>(true, "Shows fetched successfully", shows));
    }
    
    // Get upcoming shows for a movie
    @GetMapping("/upcoming/{movieId}")
    public ResponseEntity<ApiResponse<List<Show>>> getUpcomingShows(@PathVariable int movieId) {
        List<Show> shows = showService.getUpcomingShowsByMovie(movieId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Upcoming shows fetched successfully", shows));
    }
    
    // Get available shows (with seats) for a movie
    @GetMapping("/available/{movieId}")
    public ResponseEntity<ApiResponse<List<Show>>> getAvailableShows(@PathVariable int movieId) {
        List<Show> shows = showService.getAvailableShowsByMovie(movieId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Available shows fetched successfully", shows));
    }
    
    // Get shows by theater and date
    @GetMapping("/by-theater/{theaterId}")
    public ResponseEntity<ApiResponse<List<Show>>> getShowsByTheater(
            @PathVariable int theaterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<Show> shows;
        if (date != null) {
            shows = showService.getShowsByTheaterAndDate(theaterId, date);
        } else {
            shows = showService.getShowsByTheaterAndDate(theaterId, LocalDate.now());
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Shows fetched successfully", shows));
    }
    
    // Get available dates for a movie
    @GetMapping("/dates/{movieId}")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableDates(
            @PathVariable int movieId,
            @RequestParam(required = false) String city) {
        
        List<LocalDate> dates;
        if (city != null) {
            dates = showService.getAvailableDatesForMovieInCity(movieId, city);
        } else {
            dates = showService.getAvailableDatesForMovie(movieId);
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Available dates fetched successfully", dates));
    }
    
    // Update show (Admin only)
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Show>> updateShow(@PathVariable Long id, @RequestBody Show show) {
        try {
            Show updatedShow = showService.updateShow(id, show);
            return ResponseEntity.ok(new ApiResponse<>(true, "Show updated successfully", updatedShow));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Error updating show: " + e.getMessage(), null));
        }
    }
    
    // Book seats
    @PostMapping("/book/{showId}/{numberOfSeats}")
    public ResponseEntity<ApiResponse<Show>> bookSeats(
            @PathVariable Long showId,
            @PathVariable int numberOfSeats) {
        try {
            Show updatedShow = showService.bookSeats(showId, numberOfSeats);
            return ResponseEntity.ok(new ApiResponse<>(true, "Seats booked successfully", updatedShow));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error booking seats: " + e.getMessage(), null));
        }
    }
    
    // Cancel booking
    @PostMapping("/cancel/{showId}/{numberOfSeats}")
    public ResponseEntity<ApiResponse<Show>> cancelBooking(
            @PathVariable Long showId,
            @PathVariable int numberOfSeats) {
        try {
            Show updatedShow = showService.cancelBooking(showId, numberOfSeats);
            return ResponseEntity.ok(new ApiResponse<>(true, "Booking cancelled successfully", updatedShow));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error canceling booking: " + e.getMessage(), null));
        }
    }
    
    // Delete show (Admin only)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShow(@PathVariable Long id) {
        try {
            showService.deleteShow(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Show deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Error deleting show: " + e.getMessage(), null));
        }
    }
}
