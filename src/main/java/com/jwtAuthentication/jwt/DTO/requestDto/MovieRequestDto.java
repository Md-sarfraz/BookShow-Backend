package com.jwtAuthentication.jwt.DTO.requestDto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
public class MovieRequestDto {
    private int movieId;
    private String title;
    private String description;
    private String genre;
    private String format;  // Added
    private String duration;
    private String language;
    private String releaseDate;
    private String postUrl;
    private String backgroundImageUrl;
    private String rating;
    private String director;  // Added
    private String trailer;  // Added
    private List<String> castMember;  // Added
    private TheaterDto theater;
    private Boolean featured;// Added

    @Data
    public static class TheaterDto {
        private int id;  // Matches the JSON structure with capital "I"
    }
}
