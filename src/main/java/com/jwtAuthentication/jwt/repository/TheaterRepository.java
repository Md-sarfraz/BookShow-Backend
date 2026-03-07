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
    @Query("SELECT DISTINCT t FROM Theater t JOIN Show s ON s.theater.id = t.id WHERE s.movie.movieId = :movieId AND t.city.id = :cityId")
    List<Theater> findTheatersByMovieIdAndCityId(@Param("movieId") Integer movieId, @Param("cityId") Integer cityId);

    @Query("SELECT t FROM Theater t WHERE t.city.id = :cityId")
    List<Theater> findByCityId(@Param("cityId") Integer cityId);

}
