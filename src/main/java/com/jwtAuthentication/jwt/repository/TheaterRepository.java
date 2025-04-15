package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater,Integer> {
    @Query("SELECT t FROM Theater t JOIN t.movies m WHERE m.movieId = :movieId")
    List<Theater> findTheatersByMovieId(@Param("movieId") Integer movieId);


}
