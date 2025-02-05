package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.requestDto.MovieRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.MovieResponseDto;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public String deleteMovie(int id) {
        movieRepository.deleteById(id);
        return"movie deleted successfully "+id;
    }

    public MovieResponseDto updateMovie(MovieRequestDto movieRequestDto, int movieId) {
       Optional<Movie> optionalMovie = movieRepository.findById(movieId);
       if (optionalMovie.isPresent()) {
           Movie updatedMovie = optionalMovie.get();
           updatedMovie.setMovieId(movieRequestDto.getMovieId());
           updatedMovie.setTitle(movieRequestDto.getTitle());
           updatedMovie.setDescription(movieRequestDto.getDescription());
           updatedMovie.setGenre(movieRequestDto.getGenre());
           updatedMovie.setDuration(movieRequestDto.getDuration());
           updatedMovie.setLanguage(movieRequestDto.getLanguage());
           updatedMovie.setReleaseDate(movieRequestDto.getReleaseDate());
           updatedMovie.setPostUrl(movieRequestDto.getPostUrl());
           updatedMovie.setRating(movieRequestDto.getRating());
           movieRepository.save(updatedMovie);

           MovieResponseDto movieResponseDto = new MovieResponseDto();
           movieResponseDto.setMovieId(updatedMovie.getMovieId());
           movieResponseDto.setTitle(updatedMovie.getTitle());
           movieResponseDto.setDescription(updatedMovie.getDescription());
           movieResponseDto.setGenre(updatedMovie.getGenre());
           movieResponseDto.setDuration(updatedMovie.getDuration());
           movieResponseDto.setLanguage(updatedMovie.getLanguage());
           movieResponseDto.setReleaseDate(updatedMovie.getReleaseDate());
           movieResponseDto.setPostUrl(updatedMovie.getPostUrl());
           movieResponseDto.setRating(updatedMovie.getRating());
           return movieResponseDto;
       }
        else {
            throw new RuntimeException("Movie not found with id: " + movieId);
       }
    }
}
