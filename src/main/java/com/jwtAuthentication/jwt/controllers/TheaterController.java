package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.model.Theater;
import com.jwtAuthentication.jwt.service.TheaterService;
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
    public ResponseEntity<Theater> createTheater(@RequestBody Theater theater) {
        Theater createdTheater = theaterService.createTheater(theater);
        return ResponseEntity.ok(createdTheater);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Theater>> createBulkTheaters(@RequestBody List<Theater> theaters) {
        List<Theater> saved = theaters.stream()
                .map(theaterService::createTheater)
                .collect(Collectors.toList());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Theater>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Theater> getTheaterById(@PathVariable int id) {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTheater(@PathVariable int id) {
        return ResponseEntity.ok( theaterService.deleteTheater(id));
    }

    @GetMapping("/by-movie/{movieId}")
    public ResponseEntity<List<Theater>> getTheatersByMovieId(
            @PathVariable int movieId,
            @RequestParam(required = false) String city) {
        
        List<Theater> theaters;
        if (city != null && !city.isEmpty()) {
            theaters = theaterService.findTheatersByMovieIdAndCity(movieId, city);
        } else {
            theaters = theaterService.findTheatersByMovieId(movieId);
        }
        return ResponseEntity.ok(theaters);
    }

    @GetMapping("/by-city/{city}")
    public ResponseEntity<List<Theater>> getTheatersByCity(@PathVariable String city) {
        List<Theater> theaters = theaterService.findTheatersByCity(city);
        return ResponseEntity.ok(theaters);
    }

}
