package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater,Integer> {

    // Duplicate check: same name + location
    boolean existsByNameIgnoreCaseAndLocationIgnoreCase(String name, String location);

    // Query theaters showing a specific movie (through Show entity)
    @Query("SELECT DISTINCT t FROM Theater t JOIN Show s ON s.theater.id = t.id WHERE s.movie.movieId = :movieId")
    List<Theater> findTheatersByMovieId(@Param("movieId") Integer movieId);

    // Query theaters showing a specific movie in a specific city (through Show entity)
    @Query("SELECT DISTINCT t FROM Theater t JOIN Show s ON s.theater.id = t.id WHERE s.movie.movieId = :movieId AND t.city = :city")
    List<Theater> findTheatersByMovieIdAndCity(@Param("movieId") Integer movieId, @Param("city") String city);

    List<Theater> findByCity(String city);

    // Get all distinct city names that have at least one theater
    @Query("SELECT DISTINCT t.city FROM Theater t ORDER BY t.city")
    List<String> findAllDistinctCities();

}
