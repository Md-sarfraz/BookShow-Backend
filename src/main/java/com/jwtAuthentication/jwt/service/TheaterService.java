package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Theater;
import com.jwtAuthentication.jwt.repository.MovieRepository;
import com.jwtAuthentication.jwt.repository.ShowRepository;
import com.jwtAuthentication.jwt.repository.TheaterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheaterService {
    private final TheaterRepository theaterRepository;
    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    
    public TheaterService(TheaterRepository theaterRepository, MovieRepository movieRepository, ShowRepository showRepository) {
        this.theaterRepository = theaterRepository;
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
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
        boolean hasShows = showRepository.existsByTheaterId(id);
        if (hasShows) {
            throw new IllegalStateException("Cannot delete theater. Shows are still scheduled for it.");
        }
        theaterRepository.deleteById(id);
        return "Successfully deleted theater with id: " + id;
    }


    public List<Theater> findTheatersByMovieId(Integer movieId) {
        return theaterRepository.findTheatersByMovieId(movieId);
    }

    public List<Theater> findTheatersByMovieIdAndCity(Integer movieId, String city) {
        return theaterRepository.findTheatersByMovieIdAndCity(movieId, city);
    }

    public List<Theater> findTheatersByCity(String city) {
        return theaterRepository.findByCity(city);
    }

}
