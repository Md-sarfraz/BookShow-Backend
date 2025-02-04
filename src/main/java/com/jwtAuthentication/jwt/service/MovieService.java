package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.repository.MovieRepository;
import org.springframework.stereotype.Service;

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
}
