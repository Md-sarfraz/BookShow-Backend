package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Theater;
import com.jwtAuthentication.jwt.repository.MovieRepository;
import com.jwtAuthentication.jwt.repository.TheaterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheaterService {
    private final TheaterRepository theaterRepository;
    private final MovieRepository movieRepository;
    public TheaterService(TheaterRepository theaterRepository, MovieRepository movieRepository) {
        this.theaterRepository = theaterRepository;
        this.movieRepository = movieRepository;
    }

    public Theater createTheater(Theater theater) {
        return theaterRepository.save(theater);
    }

    public List<Theater> getAllTheaters() {
        return theaterRepository.findAll();
    }

    public Theater getTheaterById(int id) {
        return theaterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found with id: " + id));
    }

    public String deleteTheater(int id) {
        boolean hasMovies = movieRepository.existsByTheatersId(id);
        if (hasMovies) {
            throw new IllegalStateException("Cannot delete theater. Movies are still assigned to it.");
        }
        theaterRepository.deleteById(id);
        return "Successfully deleted theater with id: " + id;
    }


    public List<Theater> findTheatersByMovieId(Integer movieId) {
        return theaterRepository.findTheatersByMovieId(movieId);
    }

}
