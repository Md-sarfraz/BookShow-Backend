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
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable int id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Movie deleted successfully", null));
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
    public ResponseEntity<ApiResponse<List<Movie>>> findAllMovie() {
        List<Movie> movies = movieService.findAllMovie();
        return ResponseEntity.ok(new ApiResponse<>(true, "Movies fetched successfully", movies));
    }

    @GetMapping("/findMovieById/{id}")
    public ResponseEntity<ApiResponse<Movie>> findMovieById(@PathVariable int id) {
        Movie movie = movieService.findMovieById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Movie fetched successfully", movie));
    }
@GetMapping("/searchByTitle")
    public ResponseEntity<ApiResponse<List<Movie>>> searchMoviesByTitle(String title) {
        List<Movie> movies = movieService.searchMoviesByTitle(title);
        return ResponseEntity.ok(new ApiResponse<>(true, "Search results fetched successfully", movies));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<Movie>>> filterMovies(@RequestParam(required = false) String language,
                                                                  @RequestParam(required = false) String genre,
                                                                  @RequestParam(required = false) String format) {
        List<Movie> movies = movieService.filterMovies(language, genre, format);
        return ResponseEntity.ok(new ApiResponse<>(true, "Filtered movies fetched successfully", movies));
    }




//    @GetMapping("/featured")
//    public List<Movie> getTopFeaturedMovies() {
//        return movieService.getTopFeaturedMovies();
//    }


    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<List<Movie>>> topRatedMovies() {
        List<Movie> movies = movieService.getTopRatedMovies();
        return ResponseEntity.ok(new ApiResponse<>(true, "Top rated movies retrieved successfully", movies));
    }

    // 🔥 Trending
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<Movie>>> trendingMovies() {
        List<Movie> movies = movieService.getTrendingMovies();
        return ResponseEntity.ok(new ApiResponse<>(true, "Trending movies retrieved successfully", movies));
    }

    // 🎟 Popular
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<Movie>>> popularMovies() {
        List<Movie> movies = movieService.getPopularMovies();
        return ResponseEntity.ok(new ApiResponse<>(true, "Popular movies retrieved successfully", movies));
    }

    // 👁 Increase views
    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Void>> increaseView(@PathVariable Long id) {
        movieService.increaseView(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "View count increased", null));
    }

    // 🎟 Increase bookings
    @PostMapping("/{id}/book")
    public ResponseEntity<ApiResponse<Void>> increaseBooking(@PathVariable Long id) {
        movieService.increaseBooking(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Booking count increased", null));
    }

    @PatchMapping("/{id}/feature")
    public ResponseEntity<ApiResponse<Movie>> featureMovie(@PathVariable int id) {
        Movie movie = movieService.featureMovie(id);
        ApiResponse<Movie> response = new ApiResponse<>(true, "Movie marked as featured", movie);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/createBulk")
    public ResponseEntity<ApiResponse<List<String>>> createBulkMovies(@RequestBody List<MovieRequestDto> movies) {
        List<String> results = new java.util.ArrayList<>();
        int successCount = 0;
        for (MovieRequestDto dto : movies) {
            try {
                movieService.saveMovieWithUrls(dto);
                results.add("SUCCESS: " + dto.getTitle());
                successCount++;
            } catch (Exception e) {
                results.add("FAILED: " + dto.getTitle() + " - " + e.getMessage());
            }
        }
        boolean allSuccess = successCount == movies.size();
        String message = successCount + "/" + movies.size() + " movies created successfully";
        return ResponseEntity.status(allSuccess ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS)
                .body(new ApiResponse<>(allSuccess, message, results));
    }
//
}
