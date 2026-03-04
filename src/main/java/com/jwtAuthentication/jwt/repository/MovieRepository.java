package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie,Integer> {

    // Search by title (Case Insensitive)
    List<Movie> findByTitleContainingIgnoreCase(String title);

    // Filter by genre
//    List<Movie> findByGenre(String genre);
////
////    // Filter by language
//    List<Movie> findByLanguage(String language);
//    List<Movie> findByLanguageAndGenre(String language, String genre);
    List<Movie> findByLanguageAndGenre(String language, String genre);
    List<Movie> findByLanguage(String language);
    List<Movie> findByGenre(String genre);
    List<Movie> findAll();

    // Filter movies by show format (queries through Show entity)
    @Query("SELECT DISTINCT m FROM Movie m JOIN Show s ON s.movie.movieId = m.movieId WHERE s.format = :format")
    List<Movie> findByShowFormat(String format);

    @Query("SELECT DISTINCT m FROM Movie m JOIN Show s ON s.movie.movieId = m.movieId WHERE m.language = :language AND s.format = :format")
    List<Movie> findByLanguageAndShowFormat(String language, String format);

    @Query("SELECT DISTINCT m FROM Movie m JOIN Show s ON s.movie.movieId = m.movieId WHERE m.genre = :genre AND s.format = :format")
    List<Movie> findByGenreAndShowFormat(String genre, String format);

    @Query("SELECT DISTINCT m FROM Movie m JOIN Show s ON s.movie.movieId = m.movieId WHERE m.language = :language AND m.genre = :genre AND s.format = :format")
    List<Movie> findByLanguageAndGenreAndShowFormat(String language, String genre, String format);

    @Query("SELECT m FROM Movie m WHERE m.featured = true ORDER BY m.releaseDate DESC")
    List<Movie> findTopFeaturedMovies(Pageable pageable);

    // Theater-Movie connection now handled through Show entity
    List<Movie> findTop10ByOrderByViewsDesc();

    List<Movie> findTop10ByOrderByBookingsDesc();

    List<Movie> findTop10ByOrderByRatingDesc();


    // Find movies released after a specific date
//    List<Movie> findByReleaseDateAfter(LocalDate date);
//
//    // Find movies with rating greater than or equal to given rating
//    List<Movie> findByRatingGreaterThanEqual(double rating);
}
