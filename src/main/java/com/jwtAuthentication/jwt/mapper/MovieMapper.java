package com.jwtAuthentication.jwt.mapper;

import com.jwtAuthentication.jwt.DTO.requestDto.MovieRequestDto;
import com.jwtAuthentication.jwt.model.Movie;
import com.jwtAuthentication.jwt.model.Theater;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ToString
public class MovieMapper {

    /**
     * Maps MovieRequestDto to Movie entity
     * 
     * @param dto the MovieRequestDto to convert
     * @return the Movie entity
     */
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
        
        // Handle theater conversion
        if (dto.getTheater() != null) {
            Theater theater = new Theater();
            theater.setId(dto.getTheater().getId()); // Using setTheaterId, adjust if your field is named differently
            movie.setTheater(theater);
        }
        
        return movie;
    }
    
    /**
     * Maps Movie entity to MovieRequestDto
     * 
     * @param entity the Movie entity to convert
     * @return the MovieRequestDto
     */
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
        
        // Handle theater conversion
        if (entity.getTheater() != null) {
            MovieRequestDto.TheaterDto theaterDto = new MovieRequestDto.TheaterDto();
            theaterDto.setId(entity.getTheater().getId()); // Using getTheaterId, adjust if your field is named differently
            dto.setTheater(theaterDto);
        }
        
        return dto;
    }
    
    /**
     * Maps a list of Movie entities to a list of MovieRequestDtos
     * 
     * @param entities the list of Movie entities
     * @return the list of MovieRequestDtos
     */
    public List<MovieRequestDto> toDtoList(List<Movie> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}