package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.model.Theater;
import com.jwtAuthentication.jwt.service.TheaterService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/theater")
public class TheaterController {
    private final TheaterService theaterService;

    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Theater>> createTheater(@RequestBody Theater theater) {
        Theater createdTheater = theaterService.createTheater(theater);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Theater created successfully", createdTheater));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<Theater>>> createBulkTheaters(@RequestBody List<Theater> theaters) {
        List<Theater> saved = theaters.stream()
                .map(theaterService::createTheater)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Theaters created successfully", saved));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Theater>>> getAllTheaters() {
        List<Theater> theaters = theaterService.getAllTheaters();
        return ResponseEntity.ok(new ApiResponse<>(true, "Theaters fetched successfully", theaters));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Theater>> getTheaterById(@PathVariable int id) {
        Theater theater = theaterService.getTheaterById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Theater fetched successfully", theater));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTheater(@PathVariable int id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Theater deleted successfully", null));
    }

    @GetMapping("/by-movie/{movieId}")
    public ResponseEntity<ApiResponse<List<Theater>>> getTheatersByMovieId(
            @PathVariable int movieId,
            @RequestParam(required = false) String city) {

        List<Theater> theaters;
        if (city != null && !city.isEmpty()) {
            theaters = theaterService.findTheatersByMovieIdAndCity(movieId, city);
        } else {
            theaters = theaterService.findTheatersByMovieId(movieId);
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Theaters fetched successfully", theaters));
    }

    @GetMapping("/by-city/{city}")
    public ResponseEntity<ApiResponse<List<Theater>>> getTheatersByCity(@PathVariable String city) {
        List<Theater> theaters = theaterService.findTheatersByCity(city);
        return ResponseEntity.ok(new ApiResponse<>(true, "Theaters fetched successfully", theaters));
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getDistinctCities() {
        List<String> cities = theaterService.getDistinctCities();
        return ResponseEntity.ok(new ApiResponse<>(true, "Cities fetched successfully", cities));
    }

}
