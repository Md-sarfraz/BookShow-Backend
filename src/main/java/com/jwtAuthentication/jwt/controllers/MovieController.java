package com.jwtAuthentication.jwt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtAuthentication.jwt.DTO.requestDto.MovieRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.MovieResponseDto;
import com.jwtAuthentication.jwt.util.ApiResponse;
import com.jwtAuthentication.jwt.mapper.MovieMapper;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/movie")
public class MovieController {
    private final MovieService movieService;
    private final MovieMapper movieMapper;
    
    public MovieController(MovieService movieService, MovieMapper movieMapper) {
        this.movieService = movieService;
        this.movieMapper = movieMapper;
    }


    @Autowired
    private ObjectMapper objectMapper; // For manual JSON parsing

//    @PostMapping("/createMovie")
//    public ResponseEntity<Movie> addMovie(
//            @RequestPart("movie") String movieJson,
//            @RequestPart("image") MultipartFile imageFile) {
//        try {
//            // Convert movie JSON String to Movie object
//            System.out.println("this is movie" + movieJson);
//
//            Movie movie = objectMapper.readValue(movieJson, Movie.class);
////            movie.setTheater();
//
//            // Save movie with image
//            Movie savedMovie = movieService.saveMovie(movie, imageFile);
//            return ResponseEntity.ok(savedMovie);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body(null);
//        }
//    }
@PostMapping("/createMovie")
public ResponseEntity<ApiResponse<Void>> addMovie(
        @RequestPart("movie") String movieJson,
        @RequestPart(value = "image", required = false) MultipartFile imageFile,
        @RequestPart(value = "backgroundImage", required = false) MultipartFile backgroundImageFile) {

    try {
        MovieRequestDto movieDto = objectMapper.readValue(movieJson, MovieRequestDto.class);
        Movie mapped = movieMapper.toEntity(movieDto);

        movieService.saveMovie(mapped, imageFile, backgroundImageFile);

        ApiResponse<Void> response =
                new ApiResponse<>(true, "Movie created successfully", null);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (Exception e) {

        ApiResponse<Void> response =
                new ApiResponse<>(false, "Failed to create movie: " + e.getMessage(), null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


    @DeleteMapping("/deleteMovie/{id}")
    public String deleteMovie( @PathVariable int id) {
        return movieService.deleteMovie(id);
    }
    @PutMapping("/update/{movieId}")
    public ResponseEntity<ApiResponse<MovieResponseDto>> updateMovie(@RequestBody MovieRequestDto movieRequestDto, @PathVariable int movieId) {
        MovieResponseDto updatedMovie = movieService.updateMovie(movieRequestDto, movieId);

        ApiResponse<MovieResponseDto> response = new ApiResponse<MovieResponseDto>(
                true,
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

    @GetMapping("/filter")
    public List<Movie> filterMovies(@RequestParam(required = false) String language,
                                    @RequestParam(required = false) String genre,
                                    @RequestParam(required = false) String format) {
        return movieService.filterMovies(language, genre, format);
    }




//    @GetMapping("/featured")
//    public List<Movie> getTopFeaturedMovies() {
//        return movieService.getTopFeaturedMovies();
//    }


    @GetMapping("/top-rated")
    public ApiResponse<List<Movie>> topRatedMovies() {
        List<Movie> movies = movieService.getTopRatedMovies();
        return new ApiResponse<List<Movie>>(true, "Top rated movies retrieved successfully", movies);
    }

    // 🔥 Trending
    @GetMapping("/trending")
    public ApiResponse<List<Movie>> trendingMovies() {
        List<Movie> movies = movieService.getTrendingMovies();
        return new ApiResponse<List<Movie>>(true, "Trending movies retrieved successfully", movies);
    }

    // 🎟 Popular
    @GetMapping("/popular")
    public ApiResponse<List<Movie>> popularMovies() {
        List<Movie> movies = movieService.getPopularMovies();
        return new ApiResponse<List<Movie>>(true, "Popular movies retrieved successfully", movies);
    }

    // 👁 Increase views
    @PostMapping("/{id}/view")
    public ResponseEntity<String> increaseView(@PathVariable Long id) {
        movieService.increaseView(id);
        return ResponseEntity.ok("View count increased");
    }

    // 🎟 Increase bookings
    @PostMapping("/{id}/book")
    public ResponseEntity<String> increaseBooking(@PathVariable Long id) {
        movieService.increaseBooking(id);
        return ResponseEntity.ok("Booking count increased");
    }

    @PatchMapping("/{id}/feature")
    public ResponseEntity<ApiResponse<Movie>> featureMovie(@PathVariable int id) {
        Movie movie = movieService.featureMovie(id);
        ApiResponse<Movie> response = new ApiResponse<>(true, "Movie marked as featured", movie);
        return ResponseEntity.ok(response);
    }
//
}
