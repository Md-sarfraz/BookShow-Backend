package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie,Integer> {

    // Search by title (Case Insensitive)
    List<Movie> findByTitleContainingIgnoreCase(String title);

    // Filter by genre
//    List<Movie> findByGenre(String genre);
//
//    // Filter by language
//    List<Movie> findByLanguage(String language);
//
//    // Find movies released after a specific date
//    List<Movie> findByReleaseDateAfter(LocalDate date);
//
//    // Find movies with rating greater than or equal to given rating
//    List<Movie> findByRatingGreaterThanEqual(double rating);
}
