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
        movie.setPrice(dto.getPrice());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setPostUrl(dto.getPostUrl());
        movie.setRating(dto.getRating());
        movie.setDirector(dto.getDirector());
        movie.setTrailer(dto.getTrailer());
        movie.setCastMember(dto.getCastMember());
        movie.setIsFeatured(dto.getFeatured());
        movie.setBackgroundImageUrl(dto.getBackgroundImageUrl());

        // Handle theaters (many-to-many)
        if (dto.getTheaters() != null) {
            List<Theater> theaters = dto.getTheaters().stream().map(theaterDto -> {
                Theater theater = new Theater();
                theater.setId(theaterDto.getId());
                return theater;
            }).collect(Collectors.toList());
            movie.setTheaters(theaters);
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
        dto.setPrice(entity.getPrice());
        dto.setReleaseDate(entity.getReleaseDate());
        dto.setPostUrl(entity.getPostUrl());
        dto.setRating(entity.getRating());
        dto.setDirector(entity.getDirector());
        dto.setTrailer(entity.getTrailer());
        dto.setCastMember(entity.getCastMember());
        dto.setFeatured(entity.getIsFeatured());
        dto.setBackgroundImageUrl(entity.getBackgroundImageUrl());

        // Handle theaters (many-to-many)
        if (entity.getTheaters() != null) {
            List<MovieRequestDto.TheaterDto> theaterDtos = entity.getTheaters().stream().map(theater -> {
                MovieRequestDto.TheaterDto theaterDto = new MovieRequestDto.TheaterDto();
                theaterDto.setId(theater.getId());
                return theaterDto;
            }).collect(Collectors.toList());
            dto.setTheaters(theaterDtos);
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
