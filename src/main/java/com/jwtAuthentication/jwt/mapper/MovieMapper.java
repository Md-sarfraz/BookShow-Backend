package com.jwtAuthentication.jwt.mapper;

import com.jwtAuthentication.jwt.DTO.requestDto.MovieRequestDto;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.model.Theater;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ToString
public class MovieMapper {

    public Movie toEntity(MovieRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Movie movie = new Movie();
        movie.setMovieId(dto.getMovieId());
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setGenre(dto.getGenre());
        movie.setFormat(dto.getFormat());
        movie.setDuration(dto.getDuration());
        movie.setLanguage(dto.getLanguage());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setPostUrl(dto.getPostUrl());
        movie.setRating(dto.getRating());
        movie.setDirector(dto.getDirector());
        movie.setTrailer(dto.getTrailer());
        movie.setCastMember(dto.getCastMember());
        movie.setIsFeatured(dto.getFeatured()); // <-- Added this line

        // Handle theater conversion
        if (dto.getTheater() != null) {
            Theater theater = new Theater();
            theater.setId(dto.getTheater().getId());
            movie.setTheater(theater);
        }

        return movie;
    }

    public MovieRequestDto toDto(Movie entity) {
        if (entity == null) {
            return null;
        }

        MovieRequestDto dto = new MovieRequestDto();
        dto.setMovieId(entity.getMovieId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setGenre(entity.getGenre());
        dto.setFormat(entity.getFormat());
        dto.setDuration(entity.getDuration());
        dto.setLanguage(entity.getLanguage());
        dto.setReleaseDate(entity.getReleaseDate());
        dto.setPostUrl(entity.getPostUrl());
        dto.setRating(entity.getRating());
        dto.setDirector(entity.getDirector());
        dto.setTrailer(entity.getTrailer());
        dto.setCastMember(entity.getCastMember());
        dto.setFeatured(entity.getIsFeatured()); // <-- Added this line

        // Handle theater conversion
        if (entity.getTheater() != null) {
            MovieRequestDto.TheaterDto theaterDto = new MovieRequestDto.TheaterDto();
            theaterDto.setId(entity.getTheater().getId());
            dto.setTheater(theaterDto);
        }

        return dto;
    }

    public List<MovieRequestDto> toDtoList(List<Movie> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
