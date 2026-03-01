package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieResponseDto {
    private int movieId;
    private String title;
    private String description;
    private String genre;
    private String duration;
    private String language;
    private Double price;
    private String releaseDate;
    private String postUrl;
    private Double rating;
    private Boolean featured;
}
