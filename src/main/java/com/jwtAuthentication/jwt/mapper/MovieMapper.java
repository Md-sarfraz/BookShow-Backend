package com.jwtAuthentication.jwt.mapper;

import com.jwtAuthentication.jwt.DTO.requestDto.MovieRequestDto;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.model.Theater;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
        movie.setDuration(dto.getDuration());
        movie.setLanguage(dto.getLanguage());
        movie.setPrice(dto.getPrice());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setPostUrl(dto.getPostUrl());
        movie.setRating(dto.getRating());
        movie.setDirector(dto.getDirector());
        movie.setTrailer(dto.getTrailer());
        movie.setCastMember(dto.getCastMember());
        movie.setCrewMember(dto.getCrewMember());
        movie.setFeatured(dto.getFeatured());
        movie.setBackgroundImageUrl(dto.getBackgroundImageUrl());

        // Movies are now connected to theaters through Show entity, not directly
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
        dto.setDuration(entity.getDuration());
        dto.setLanguage(entity.getLanguage());
        dto.setPrice(entity.getPrice());
        dto.setReleaseDate(entity.getReleaseDate());
        dto.setPostUrl(entity.getPostUrl());
        dto.setRating(entity.getRating());
        dto.setDirector(entity.getDirector());
        dto.setTrailer(entity.getTrailer());
        dto.setCastMember(entity.getCastMember());
        dto.setCrewMember(entity.getCrewMember());
        dto.setFeatured(entity.getFeatured());
        dto.setBackgroundImageUrl(entity.getBackgroundImageUrl());

        // Movies are now connected to theaters through Show entity, not directly
        return dto;
    }

    public List<MovieRequestDto> toDtoList(List<Movie> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
