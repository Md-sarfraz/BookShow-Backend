package com.jwtAuthentication.jwt.DTO.requestDto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;


@Data
public class MovieRequestDto {
    private int movieId;
    private String title;
    private String description;
    private String genre;
    private String duration;
    private String language;
    private String releaseDate;
    private String postUrl;
    private String rating;

}
