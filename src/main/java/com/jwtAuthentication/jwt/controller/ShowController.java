package com.jwtAuthentication.jwt.controller;

import com.jwtAuthentication.jwt.model.Show;
import com.jwtAuthentication.jwt.service.ShowService;
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
    public ResponseEntity<?> createShow(@RequestBody Show show) {
        try {
            Show createdShow = showService.createShow(show);
            return ResponseEntity.ok(createdShow);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating show: " + e.getMessage());
        }
    }
    
    // Create show by movie and theater IDs
    @PostMapping("/create/{movieId}/{theaterId}")
    public ResponseEntity<?> createShowByIds(
            @PathVariable int movieId,
            @PathVariable int theaterId,
            @RequestBody Show show) {
        try {
            Show createdShow = showService.createShow(movieId, theaterId, show);
            return ResponseEntity.ok(createdShow);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating show: " + e.getMessage());
        }
    }
    
    // Get all shows
    @GetMapping
    public ResponseEntity<List<Show>> getAllShows() {
        List<Show> shows = showService.getAllShows();
        return ResponseEntity.ok(shows);
    }
    
    // Get show by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getShowById(@PathVariable Long id) {
        Optional<Show> show = showService.getShowById(id);
        if (show.isPresent()) {
            return ResponseEntity.ok(show.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Show not found");
    }
    
    // Get shows by movie (with optional city and date filters)
    @GetMapping("/by-movie/{movieId}")
    public ResponseEntity<List<Show>> getShowsByMovie(
            @PathVariable int movieId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        System.out.println("========================================");
        System.out.println("📋 GET /api/v1/shows/by-movie/" + movieId);
        System.out.println("   Request Params:");
        System.out.println("      city: " + (city != null ? "'" + city + "'" : "null"));
        System.out.println("      date: " + (date != null ? date : "null"));
        System.out.println("========================================");
        
        List<Show> shows;
        
        if (city != null && date != null) {
            System.out.println("🔀 Route: city + date");
            shows = showService.getShowsByMovieAndCityAndDate(movieId, city, date);
        } else if (city != null) {
            System.out.println("🔀 Route: city only");
            shows = showService.getShowsByMovieAndCity(movieId, city);
        } else if (date != null) {
            System.out.println("🔀 Route: date only");
            shows = showService.getShowsByMovieAndDate(movieId, date);
        } else {
            System.out.println("🔀 Route: no filters");
            shows = showService.getShowsByMovie(movieId);
        }
        
        System.out.println("📤 Returning " + shows.size() + " shows to frontend");
        System.out.println("========================================");
        return ResponseEntity.ok(shows);
    }
    
    // Get upcoming shows for a movie
    @GetMapping("/upcoming/{movieId}")
    public ResponseEntity<List<Show>> getUpcomingShows(@PathVariable int movieId) {
        List<Show> shows = showService.getUpcomingShowsByMovie(movieId);
        return ResponseEntity.ok(shows);
    }
    
    // Get available shows (with seats) for a movie
    @GetMapping("/available/{movieId}")
    public ResponseEntity<List<Show>> getAvailableShows(@PathVariable int movieId) {
        List<Show> shows = showService.getAvailableShowsByMovie(movieId);
        return ResponseEntity.ok(shows);
    }
    
    // Get shows by theater and date
    @GetMapping("/by-theater/{theaterId}")
    public ResponseEntity<List<Show>> getShowsByTheater(
            @PathVariable int theaterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<Show> shows;
        if (date != null) {
            shows = showService.getShowsByTheaterAndDate(theaterId, date);
        } else {
            shows = showService.getShowsByTheaterAndDate(theaterId, LocalDate.now());
        }
        
        return ResponseEntity.ok(shows);
    }
    
    // Get available dates for a movie
    @GetMapping("/dates/{movieId}")
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @PathVariable int movieId,
            @RequestParam(required = false) String city) {
        
        List<LocalDate> dates;
        if (city != null) {
            dates = showService.getAvailableDatesForMovieInCity(movieId, city);
        } else {
            dates = showService.getAvailableDatesForMovie(movieId);
        }
        
        return ResponseEntity.ok(dates);
    }
    
    // Update show (Admin only)
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateShow(@PathVariable Long id, @RequestBody Show show) {
        try {
            Show updatedShow = showService.updateShow(id, show);
            return ResponseEntity.ok(updatedShow);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error updating show: " + e.getMessage());
        }
    }
    
    // Book seats
    @PostMapping("/book/{showId}/{numberOfSeats}")
    public ResponseEntity<?> bookSeats(
            @PathVariable Long showId,
            @PathVariable int numberOfSeats) {
        try {
            Show updatedShow = showService.bookSeats(showId, numberOfSeats);
            return ResponseEntity.ok(updatedShow);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error booking seats: " + e.getMessage());
        }
    }
    
    // Cancel booking
    @PostMapping("/cancel/{showId}/{numberOfSeats}")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long showId,
            @PathVariable int numberOfSeats) {
        try {
            Show updatedShow = showService.cancelBooking(showId, numberOfSeats);
            return ResponseEntity.ok(updatedShow);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error canceling booking: " + e.getMessage());
        }
    }
    
    // Delete show (Admin only)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteShow(@PathVariable Long id) {
        try {
            showService.deleteShow(id);
            return ResponseEntity.ok("Show deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error deleting show: " + e.getMessage());
        }
    }
}
