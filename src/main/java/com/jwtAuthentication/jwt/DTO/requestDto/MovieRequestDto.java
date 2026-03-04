package com.jwtAuthentication.jwt.DTO.requestDto;

import com.jwtAuthentication.jwt.model.Person;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MovieRequestDto {
    private int movieId;
    private String title;
    private String description;
    private String genre;
    private String duration;
    private String language;
    private Double price;
    private LocalDate releaseDate;
    private String postUrl;
    private String backgroundImageUrl;
    private Double rating;
    private String director;
    private String trailer;
    private Boolean featured;
    private List<Person> castMember;
    private List<Person> crewMember;
    private List<TheaterDto> theaters; // ✅ Updated to list of theaters

    @Data
    public static class TheaterDto {
        private int id;
        // Optionally: name, location, etc., if needed for frontend
    }
}
