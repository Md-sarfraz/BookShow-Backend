package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.requestDto.MovieRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.MovieResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.ApiResponse;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/movie")
public class MovieController {
    private final MovieService movieService;
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }
    @PostMapping("/createMovie/{theaterId}")
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie, @PathVariable int theaterId) {
        Movie savedMovie= movieService.saveMovie(movie,theaterId);
        return ResponseEntity.ok(savedMovie);
    }
    @DeleteMapping("/deleteMovie/{id}")
    public String deleteMovie( @PathVariable int id) {
        return movieService.deleteMovie(id);
    }
    @PutMapping("/update/{movieId}")
    public ResponseEntity<ApiResponse<MovieResponseDto>> updateMovie(@RequestBody MovieRequestDto movieRequestDto,@PathVariable int movieId){
        MovieResponseDto updatedMovie=movieService.updateMovie(movieRequestDto,movieId);
        ApiResponse<MovieResponseDto> response=new ApiResponse<>(
                "success",
                "Movie updated successfully",
                updatedMovie
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/findAllMovie")
    public List<Movie> findAllMovie(){
        return movieService.findAllMovie();
    }

    @GetMapping("/findMovieById/{id}")
    public Movie findMovieById(@PathVariable int id){
        return movieService.findMovieById(id);
    }
@GetMapping("/searchByTitle")
    public List<Movie> searchMoviesByTitle(String title){
        return movieService.searchMoviesByTitle(title);
    }


    @GetMapping("/filter/genre")
    public ResponseEntity<List<Movie>> filterByGenre(@RequestParam String genre) {
        return ResponseEntity.ok(movieService.filterByGenre(genre));
    }

    // Filter by language
    @GetMapping("/filter/language")
    public ResponseEntity<List<Movie>> filterByLanguage(@RequestParam String language) {
        return ResponseEntity.ok(movieService.filterByLanguage(language));
    }
//@GetMapping("/filter/releaseDate")
//    public ResponseEntity<List<Movie>>filterByReleaseDate(@RequestParam String date) {
//        LocalDate releaseDate = LocalDate.parse(date);
//        return ResponseEntity.ok(movieService.filterByReleaseDate(releaseDate));
//    }
//@GetMapping("/filter/rating")
//    public ResponseEntity<List<Movie>> filterByRating(@RequestParam Double rating) {
//        return ResponseEntity.ok(movieService.filterByRating(rating));
//    }
}
