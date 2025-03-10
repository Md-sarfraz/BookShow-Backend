package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.requestDto.MovieRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.MovieResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.ApiResponse;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.service.MovieService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/movie")
public class MovieController {
    private final MovieService movieService;
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }
//    @PostMapping(
//            value = "/createMovie/{theaterId}",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public ResponseEntity<String> createMovie(
//            @RequestPart("movie") Movie movie,
//            @RequestPart("file") MultipartFile file,
//            @PathVariable int theaterId
//    ) {
//        System.out.println("filename: " + file.getName());
//        return ResponseEntity.ok(file.getName());
//    }

    @PostMapping("/createMovie")
    public ResponseEntity<Movie> addMovie(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("genre") String genre,
            @RequestParam("format") String format,
            @RequestParam("duration") String duration,
            @RequestParam("language") String language,
            @RequestParam("releaseDate") String releaseDate,
            @RequestParam("postUrl") String postUrl,
            @RequestParam("rating") String rating,
            @RequestParam("director") String director,
            @RequestParam("trailer") String trailer,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("theaterId") int theaterId
    )
    {
        try{
           Movie savedMovie=movieService.saveMovie(title, description, genre, format, duration, language, releaseDate,postUrl, rating, director, trailer, imageFile, theaterId);
           return ResponseEntity.ok(savedMovie);
        }
        catch (Exception e) {
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
