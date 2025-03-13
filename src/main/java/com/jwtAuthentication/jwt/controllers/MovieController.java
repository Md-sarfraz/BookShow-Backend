package com.jwtAuthentication.jwt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtAuthentication.jwt.DTO.requestDto.MovieRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.MovieResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.ApiResponse;
import com.jwtAuthentication.jwt.mapper.MovieMapper;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/movie")
public class MovieController {
    private final MovieService movieService;
    private final MovieMapper movieMapper;
    public MovieController(MovieService movieService,MovieMapper movieMapper) {
        this.movieService = movieService;
        this.movieMapper = movieMapper;
    }

//    @PostMapping("/createMovie")
//    public ResponseEntity<Movie> addMovie(
//            @RequestParam("title") String title,
//            @RequestParam("description") String description,
//            @RequestParam("genre") String genre,
//            @RequestParam("format") String format,
//            @RequestParam("duration") String duration,
//            @RequestParam("language") String language,
//            @RequestParam("releaseDate") String releaseDate,
//            @RequestParam("postUrl") String postUrl,
//            @RequestParam("rating") String rating,
//            @RequestParam("director") String director,
//            @RequestParam("trailer") String trailer,
//            @RequestParam("image") MultipartFile imageFile,
//            @RequestParam("theaterId") int theaterId
//    )
//    {
//        try{
//           Movie savedMovie=movieService.saveMovie(title, description, genre, format, duration, language, releaseDate,postUrl, rating, director, trailer, imageFile, theaterId);
//           return ResponseEntity.ok(savedMovie);
//        }
//        catch (Exception e) {
//            return ResponseEntity.status(500).body(null);
//        }
//    }


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
    public ResponseEntity<Movie> addMovie(
            @RequestPart("movie") String movieJson,
            @RequestPart("image") MultipartFile imageFile) {
        try {
            // Convert movie JSON String to Movie object
            System.out.println("this is movie" + movieJson);

            MovieRequestDto movie = objectMapper.readValue(movieJson, MovieRequestDto.class);

//

            // Save movie with image
            Movie mapped = movieMapper.toEntity(movie);
//            System.out.println("This is movie mapped" +mapped);
            Movie savedMovie = movieService.saveMovie(mapped, imageFile);
            return ResponseEntity.ok(savedMovie);
//            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
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

    @GetMapping("/filter")
    public List<Movie> filterMovies(@RequestParam(required = false) String language,
                                    @RequestParam(required = false) String genre,
                                    @RequestParam(required = false) String format) {
        return movieService.filterMovies(language, genre,format);
    }
//
}
