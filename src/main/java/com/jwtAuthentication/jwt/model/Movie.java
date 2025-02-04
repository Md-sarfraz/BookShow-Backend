package com.jwtAuthentication.jwt.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString
public class Movie {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int movieId;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String genre;

    @Column
    private String duration;

    @Column
    private String language;

    @Column
    private String releaseDate;

    @Column
    private String postUrl;

    @Column
    private String rating;

}
