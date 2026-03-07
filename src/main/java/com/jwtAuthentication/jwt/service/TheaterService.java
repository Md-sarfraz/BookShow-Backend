package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.execption.DuplicateResourceException;
import com.jwtAuthentication.jwt.model.Activity;
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
    private final ActivityService activityService;
    
    public TheaterService(TheaterRepository theaterRepository, MovieRepository movieRepository, ShowRepository showRepository, ActivityService activityService) {
        this.theaterRepository = theaterRepository;
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
        this.activityService = activityService;
    }

    public Theater createTheater(Theater theater) {
        // Duplicate check: same name + location
        if (theaterRepository.existsByNameIgnoreCaseAndLocationIgnoreCase(theater.getName(), theater.getLocation())) {
            throw new DuplicateResourceException(
                    "Theater '" + theater.getName() + "' at '" + theater.getLocation() + "' already exists.");
        }

        Theater savedTheater = theaterRepository.save(theater);
        
        // Log activity
        activityService.logActivity(
            Activity.ActivityType.THEATER_ADDED,
            "New theater '" + savedTheater.getName() + "' registered in " + (savedTheater.getCity() != null ? savedTheater.getCity().getName() : "N/A"),
            savedTheater.getName(),
            Long.valueOf(savedTheater.getId()),
            "City: " + (savedTheater.getCity() != null ? savedTheater.getCity().getName() : "N/A") + " | Location: " + savedTheater.getLocation()
        );
        
        return savedTheater;
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

    public List<Theater> findTheatersByMovieIdAndCityId(Integer movieId, Integer cityId) {
        return theaterRepository.findTheatersByMovieIdAndCityId(movieId, cityId);
    }

    public List<Theater> findTheatersByCityId(Integer cityId) {
        return theaterRepository.findByCityId(cityId);
    }

}
