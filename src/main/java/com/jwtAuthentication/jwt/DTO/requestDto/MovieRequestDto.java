package com.jwtAuthentication.jwt.DTO.requestDto;

import lombok.Data;
import java.util.List;

@Data
public class MovieRequestDto {
    private int movieId;
    private String title;
    private String description;
    private String genre;
    private String format;
    private String duration;
    private String language;
    private String releaseDate;
    private String postUrl;
    private String backgroundImageUrl;
    private String rating;
    private String director;
    private String trailer;
    private List<String> castMember;
    private List<TheaterDto> theaters; // ✅ Updated to list of theaters
    private Boolean featured;

    @Data
    public static class TheaterDto {
        private int id;
        // Optionally: name, location, etc., if needed for frontend
    }
}
