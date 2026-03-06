package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.execption.DuplicateResourceException;
import com.jwtAuthentication.jwt.model.Activity;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.model.Show;
import com.jwtAuthentication.jwt.model.Theater;
import com.jwtAuthentication.jwt.repository.MovieRepository;
import com.jwtAuthentication.jwt.repository.ShowRepository;
import com.jwtAuthentication.jwt.repository.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ShowService {
    
    @Autowired
    private ShowRepository showRepository;
    
    @Autowired
    private MovieRepository movieRepository;
    
    @Autowired
    private TheaterRepository theaterRepository;
    
    @Autowired
    private ActivityService activityService;
    
    // Create a new show
    public Show createShow(Show show) {
        return showRepository.save(show);
    }
    
    // Create show by IDs
    public Show createShow(int movieId, int theaterId, Show show) {
        Optional<Movie> movie = movieRepository.findById(movieId);
        Optional<Theater> theater = theaterRepository.findById(theaterId);
        
        if (movie.isPresent() && theater.isPresent()) {
            // Duplicate check: same movie + theater + date + time + screen
            if (showRepository.existsByMovieMovieIdAndTheaterIdAndShowDateAndShowTimeAndScreenNumber(
                    movieId, theaterId, show.getShowDate(), show.getShowTime(), show.getScreenNumber())) {
                throw new DuplicateResourceException(
                        "A show for '" + movie.get().getTitle() + "' at theater '" + theater.get().getName() +
                        "' on " + show.getShowDate() + " at " + show.getShowTime() +
                        " (Screen " + show.getScreenNumber() + ") already exists.");
            }

            show.setMovie(movie.get());
            show.setTheater(theater.get());
            Show savedShow = showRepository.save(show);
            
            // Log activity
            activityService.logActivity(
                Activity.ActivityType.SHOW_CREATED,
                "Scheduled show for '" + movie.get().getTitle() + "' at " + theater.get().getName() + ", Screen " + show.getScreenNumber(),
                movie.get().getTitle(),
                savedShow.getShowId(),
                "Theater: " + theater.get().getName() + " | Date: " + show.getShowDate() + " | Time: " + show.getShowTime()
            );
            
            return savedShow;
        }
        throw new RuntimeException("Movie or Theater not found");
    }
    
    // Get all shows
    public List<Show> getAllShows() {
        return showRepository.findAll();
    }
    
    // Get show by ID
    public Optional<Show> getShowById(Long id) {
        return showRepository.findById(id);
    }
    
    // Get shows by movie
    public List<Show> getShowsByMovie(int movieId) {
        System.out.println("🔍 Getting ALL shows for movie " + movieId + " (no filters)");
        List<Show> shows = showRepository.findByMovieMovieId(movieId);
        System.out.println("✅ Found " + shows.size() + " shows total");
        if (!shows.isEmpty()) {
            shows.forEach(show -> {
                System.out.println("   📍 Show " + show.getShowId() + ": " +
                    show.getTheater().getName() + " in " + 
                    show.getTheater().getCity() + " on " + 
                    show.getShowDate());
            });
        }
        return shows;
    }
    
    // Get shows by movie and date
    public List<Show> getShowsByMovieAndDate(int movieId, LocalDate date) {
        return showRepository.findByMovieMovieIdAndShowDate(movieId, date);
    }
    
    // Get shows by movie and city
    public List<Show> getShowsByMovieAndCity(int movieId, String city) {
        return showRepository.findByMovieIdAndCity(movieId, city);
    }
    
    // Get shows by movie, city and date
    public List<Show> getShowsByMovieAndCityAndDate(int movieId, String city, LocalDate date) {
        System.out.println("========================================");
        System.out.println("🔍 ShowService.getShowsByMovieAndCityAndDate");
        System.out.println("   movieId: " + movieId);
        System.out.println("   city: '" + city + "'");
        System.out.println("   date: " + date);
        System.out.println("========================================");
        
        List<Show> shows = showRepository.findByMovieIdAndCityAndDate(movieId, city, date);
        
        System.out.println("✅ Query returned " + shows.size() + " shows");
        if (shows.isEmpty()) {
            System.out.println("⚠️ No shows found!");
            System.out.println("⚠️ Make sure theater.city = '" + city + "' in database");
        } else {
            shows.forEach(show -> {
                System.out.println("   📍 Show: " + show.getShowId() + 
                    " | Theater: " + show.getTheater().getName() + 
                    " | City: " + show.getTheater().getCity());
            });
        }
        System.out.println("========================================");
        return shows;
    }
    
    // Get upcoming shows for a movie
    public List<Show> getUpcomingShowsByMovie(int movieId) {
        return showRepository.findUpcomingShowsByMovie(movieId, LocalDate.now());
    }
    
    // Get available shows for a movie
    public List<Show> getAvailableShowsByMovie(int movieId) {
        return showRepository.findAvailableShowsByMovie(movieId, LocalDate.now());
    }
    
    // Get shows by theater and date
    public List<Show> getShowsByTheaterAndDate(int theaterId, LocalDate date) {
        return showRepository.findByTheaterIdAndShowDate(theaterId, date);
    }
    
    // Get available dates for a movie
    public List<LocalDate> getAvailableDatesForMovie(int movieId) {
        return showRepository.findDistinctDatesByMovie(movieId, LocalDate.now());
    }
    
    // Get available dates for a movie in a city
    public List<LocalDate> getAvailableDatesForMovieInCity(int movieId, String city) {
        return showRepository.findDistinctDatesByMovieAndCity(movieId, city, LocalDate.now());
    }
    
    // Update show
    public Show updateShow(Long id, Show show) {
        Optional<Show> existingShow = showRepository.findById(id);
        if (existingShow.isPresent()) {
            Show updatedShow = existingShow.get();
            updatedShow.setShowDate(show.getShowDate());
            updatedShow.setShowTime(show.getShowTime());
            updatedShow.setPrice(show.getPrice());
            updatedShow.setAvailableSeats(show.getAvailableSeats());
            updatedShow.setTotalSeats(show.getTotalSeats());
            updatedShow.setLanguage(show.getLanguage());
            updatedShow.setFormat(show.getFormat());
            return showRepository.save(updatedShow);
        }
        throw new RuntimeException("Show not found with id: " + id);
    }
    
    // Book seats (reduce available seats)
    public Show bookSeats(Long showId, int numberOfSeats) {
        Optional<Show> optionalShow = showRepository.findById(showId);
        if (optionalShow.isPresent()) {
            Show show = optionalShow.get();
            if (show.getAvailableSeats() >= numberOfSeats) {
                show.setAvailableSeats(show.getAvailableSeats() - numberOfSeats);
                return showRepository.save(show);
            }
            throw new RuntimeException("Not enough seats available");
        }
        throw new RuntimeException("Show not found with id: " + showId);
    }
    
    // Cancel booking (increase available seats)
    public Show cancelBooking(Long showId, int numberOfSeats) {
        Optional<Show> optionalShow = showRepository.findById(showId);
        if (optionalShow.isPresent()) {
            Show show = optionalShow.get();
            int newAvailableSeats = show.getAvailableSeats() + numberOfSeats;
            if (newAvailableSeats <= show.getTotalSeats()) {
                show.setAvailableSeats(newAvailableSeats);
                return showRepository.save(show);
            }
            throw new RuntimeException("Cannot exceed total seats");
        }
        throw new RuntimeException("Show not found with id: " + showId);
    }
    
    // Delete show
    public void deleteShow(Long id) {
        showRepository.deleteById(id);
    }
}
