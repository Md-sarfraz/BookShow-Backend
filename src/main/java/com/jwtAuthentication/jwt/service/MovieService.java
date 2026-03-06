package com.jwtAuthentication.jwt.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jwtAuthentication.jwt.DTO.requestDto.MovieRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.MovieResponseDto;
import com.jwtAuthentication.jwt.execption.DuplicateResourceException;
import com.jwtAuthentication.jwt.model.Activity;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.model.Theater;
import com.jwtAuthentication.jwt.repository.MovieRepository;
import com.jwtAuthentication.jwt.repository.TheaterRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MovieService {
    private final TheaterRepository theaterRepository;
    private final MovieRepository movieRepository;
    private final Cloudinary cloudinary;
    private final ActivityService activityService;
    
    public MovieService(MovieRepository movieRepository, TheaterRepository theaterRepository, Cloudinary cloudinary, ActivityService activityService) {
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.cloudinary = cloudinary;
        this.activityService = activityService;
    }


    public Movie saveMovie(Movie movie, MultipartFile imageFile, MultipartFile backgroundImageFile) throws IOException {
        // Duplicate check: same title + language
        if (movieRepository.existsByTitleIgnoreCaseAndLanguageIgnoreCase(movie.getTitle(), movie.getLanguage())) {
            throw new DuplicateResourceException(
                    "Movie '" + movie.getTitle() + "' in language '" + movie.getLanguage() + "' already exists.");
        }

        // Upload main image to Cloudinary
        Map uploadResultMain = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
        String imageUrl = uploadResultMain.get("secure_url").toString();
        movie.setPostUrl(imageUrl);

        // Upload background image to Cloudinary
        Map uploadResultBg = cloudinary.uploader().upload(backgroundImageFile.getBytes(), ObjectUtils.emptyMap());
        String backgroundImageUrl = uploadResultBg.get("secure_url").toString();
        movie.setBackgroundImageUrl(backgroundImageUrl);

        // Movies are now connected to theaters through Show entity, not directly
        Movie savedMovie = movieRepository.save(movie);
        
        // Log activity
        activityService.logActivity(
            Activity.ActivityType.MOVIE_ADDED,
            "Added a new movie '" + savedMovie.getTitle() + "' to the catalog",
            savedMovie.getTitle(),
            Long.valueOf(savedMovie.getMovieId()),
            savedMovie.getGenre() + " | " + savedMovie.getLanguage()
        );
        
        return savedMovie;
    }





    public String deleteMovie(int id) {
        Optional<Movie> movie = movieRepository.findById(id);

        if (movie.isPresent()) {
            movieRepository.deleteById(id);
            return "Movie deleted successfully: " + id;
        } else {
            return "Movie not found with ID: " + id;
        }
    }

    public MovieResponseDto updateMovie(MovieRequestDto movieRequestDto, int movieId) {
        Optional<Movie> optionalMovie = movieRepository.findById(movieId);

        if (optionalMovie.isPresent()) {
            Movie updatedMovie = optionalMovie.get();

            updatedMovie.setTitle(movieRequestDto.getTitle());
            updatedMovie.setDescription(movieRequestDto.getDescription());
            updatedMovie.setGenre(movieRequestDto.getGenre());
            updatedMovie.setDuration(movieRequestDto.getDuration());
            updatedMovie.setLanguage(movieRequestDto.getLanguage());
            updatedMovie.setPrice(movieRequestDto.getPrice());
            updatedMovie.setReleaseDate(movieRequestDto.getReleaseDate());
            updatedMovie.setPostUrl(movieRequestDto.getPostUrl());
            updatedMovie.setRating(movieRequestDto.getRating());
            updatedMovie.setFeatured(movieRequestDto.getFeatured());  // <-- Added this line

            movieRepository.save(updatedMovie);

            MovieResponseDto movieResponseDto = new MovieResponseDto();
            movieResponseDto.setMovieId(updatedMovie.getMovieId());
            movieResponseDto.setTitle(updatedMovie.getTitle());
            movieResponseDto.setDescription(updatedMovie.getDescription());
            movieResponseDto.setGenre(updatedMovie.getGenre());
            movieResponseDto.setDuration(updatedMovie.getDuration());
            movieResponseDto.setLanguage(updatedMovie.getLanguage());
            movieResponseDto.setPrice(updatedMovie.getPrice());
            movieResponseDto.setReleaseDate(String.valueOf(updatedMovie.getReleaseDate()));
            movieResponseDto.setPostUrl(updatedMovie.getPostUrl());
            movieResponseDto.setRating(updatedMovie.getRating());
            movieResponseDto.setFeatured(updatedMovie.getFeatured());  // <-- Added this line

            return movieResponseDto;
        } else {
            throw new RuntimeException("Movie not found with id: " + movieId);
        }
    }


    public List<Movie> findAllMovie() {
       return movieRepository.findAll();
    }

    public Movie findMovieById(int id) {
          Optional<Movie> movie = movieRepository.findById(id);
           if(movie.isPresent()) {
            return movie.get();
           } else {
               throw new RuntimeException("Movie not found with id: " + id);
       }
    }

    public List<Movie> searchMoviesByTitle(@RequestParam String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Movie> filterByGenre(String genre) {
        return movieRepository.findByGenre(genre);
    }

    public List<Movie> filterByLanguage(String language) {
        return movieRepository.findByLanguage(language);
    }



    public List<Movie> filterMovies(String language, String genre, String format) {
        if (language != null && genre != null && format != null) {
            return movieRepository.findByLanguageAndGenreAndShowFormat(language, genre, format);
        } else if (language != null && format != null) {
            return movieRepository.findByLanguageAndShowFormat(language, format);
        } else if (genre != null && format != null) {
            return movieRepository.findByGenreAndShowFormat(genre, format);
        } else if (format != null) {
            return movieRepository.findByShowFormat(format);
        } else if (language != null && genre != null) {
            return movieRepository.findByLanguageAndGenre(language, genre);
        } else if (language != null) {
            return movieRepository.findByLanguage(language);
        } else if (genre != null) {
            return movieRepository.findByGenre(genre);
        } else {
            return movieRepository.findAll();
        }
    }

//    public List<Movie> getTopFeaturedMovies() {
//        // Fetching the top 5 featured movies (you can change the number in PageRequest)
//        Pageable topFive = PageRequest.of(0, 5);  // This returns the first 5 featured movies
//        return movieRepository.findTopFeaturedMovies((java.awt.print.Pageable) topFive);
//    }




//    public List<Movie> filterByReleaseDate(LocalDate date) {
//         return movieRepository.findByReleaseDateAfter(date);
//    }
//
//
//    public List<Movie> filterByRating(Double rating) {
//        return movieRepository.findByRatingGreaterThanEqual(rating);
//    }

    public List<Movie> getTrendingMovies() {
        return movieRepository.findTop10ByOrderByViewsDesc();
    }

    // 🎟 Popular = highest bookings
    public List<Movie> getPopularMovies() {
        return movieRepository.findTop10ByOrderByBookingsDesc();
    }

    // ⭐ Top Rated = highest rating
    public List<Movie> getTopRatedMovies() {
        return movieRepository.findTop10ByOrderByRatingDesc();
    }

    // 👁 Increase views
    public void increaseView(Long movieId) {
        Movie movie = movieRepository.findById(Math.toIntExact(movieId))
                .orElseThrow(() -> new RuntimeException("Movie not found with id " + movieId));

        movie.setViews(movie.getViews() + 1);
        movieRepository.save(movie);
    }

    // 🎟 Increase bookings
    public void increaseBooking(Long movieId) {
        Movie movie = movieRepository.findById(Math.toIntExact(movieId))
                .orElseThrow(() -> new RuntimeException("Movie not found with id " + movieId));

        movie.setBookings(movie.getBookings() + 1);
        movieRepository.save(movie);
    }

    // ⭐ Feature movie
    public Movie featureMovie(int movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id " + movieId));
        
        movie.setFeatured(true);
        return movieRepository.save(movie);
    }

}
