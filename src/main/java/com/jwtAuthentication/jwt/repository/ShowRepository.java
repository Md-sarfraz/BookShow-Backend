package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    // Duplicate check: same movie + theater + date + time + screen
    boolean existsByMovieMovieIdAndTheaterIdAndShowDateAndShowTimeAndScreenNumber(
            int movieId, int theaterId, LocalDate showDate, LocalTime showTime, String screenNumber);

    // Find shows by movie ID
    List<Show> findByMovieMovieId(int movieId);
    
    // Find shows by movie and date
    List<Show> findByMovieMovieIdAndShowDate(int movieId, LocalDate showDate);
    
    // Find shows by movie, theater and date
    List<Show> findByMovieMovieIdAndTheaterIdAndShowDate(int movieId, int theaterId, LocalDate showDate);
    
    // Find shows by movie and cityId
    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId AND s.theater.city.id = :cityId")
    List<Show> findByMovieIdAndCityId(@Param("movieId") int movieId, @Param("cityId") Integer cityId);
    
    // Find shows by movie, cityId and date
    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId AND s.theater.city.id = :cityId AND s.showDate = :date")
    List<Show> findByMovieIdAndCityIdAndDate(@Param("movieId") int movieId, @Param("cityId") Integer cityId, @Param("date") LocalDate date);
    
    // Find upcoming shows for a movie
    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId AND s.showDate >= :fromDate ORDER BY s.showDate, s.showTime")
    List<Show> findUpcomingShowsByMovie(@Param("movieId") int movieId, @Param("fromDate") LocalDate fromDate);
    
    // Find shows by theater and date
    List<Show> findByTheaterIdAndShowDate(int theaterId, LocalDate showDate);
    
    // Find available shows (with seats)
    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId AND s.availableSeats > 0 AND s.showDate >= :fromDate")
    List<Show> findAvailableShowsByMovie(@Param("movieId") int movieId, @Param("fromDate") LocalDate fromDate);
    
    // Get distinct dates for a movie
    @Query("SELECT DISTINCT s.showDate FROM Show s WHERE s.movie.movieId = :movieId AND s.showDate >= :fromDate ORDER BY s.showDate")
    List<LocalDate> findDistinctDatesByMovie(@Param("movieId") int movieId, @Param("fromDate") LocalDate fromDate);
    
    // Get distinct dates for a movie in a city
    @Query("SELECT DISTINCT s.showDate FROM Show s WHERE s.movie.movieId = :movieId AND s.theater.city.id = :cityId AND s.showDate >= :fromDate ORDER BY s.showDate")
    List<LocalDate> findDistinctDatesByMovieAndCityId(@Param("movieId") int movieId, @Param("cityId") Integer cityId, @Param("fromDate") LocalDate fromDate);

    // Count today's shows
    long countByShowDate(LocalDate showDate);
    
    // Check if theater has any shows
    boolean existsByTheaterId(int theaterId);
}
