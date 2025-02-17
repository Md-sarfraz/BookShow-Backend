package com.jwtAuthentication.jwt.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

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
    private LocalDate releaseDate;

    @Column
    private String postUrl;

    @Column
    private String rating;
    @Column

    private String director;

    @ElementCollection
    @CollectionTable(name = "movie_cast", joinColumns = @JoinColumn(name = "movie_id"))
    @Column
    private List<String>castMember;


    @ManyToOne
    @JoinColumn
    private Theater theater;
}
