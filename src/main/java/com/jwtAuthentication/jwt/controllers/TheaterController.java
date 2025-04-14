package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.model.Theater;
import com.jwtAuthentication.jwt.service.TheaterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/theater")
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

//    @GetMapping("/by-movie/{movieId}")
//    public ResponseEntity<List<Theater>> getTheatersByMovie(@PathVariable int movieId) {
//        List<Theater> theaters = theaterService.getTheatersByMovie(movieId);
//        return ResponseEntity.ok(theaters);
//    }



}
