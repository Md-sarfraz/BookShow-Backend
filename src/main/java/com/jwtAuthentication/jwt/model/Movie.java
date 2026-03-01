package com.jwtAuthentication.jwt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
//import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
//import jakarta.validation.constraints.Pattern;


import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@ToString(exclude = "theater")
public class Movie {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int movieId;

    @Column
//    @NotBlank(message = "title is required")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String genre;

    @Column
    private String format;

    @Column
    private String duration;

    @Column
    private String language;

    @Column
    private Double price;

    @Column
    private LocalDate releaseDate;

    @Column
    private String postUrl;
    @Column
    private Boolean featured;

    @Column
    private String backgroundImageUrl;


    @Column
    private Double rating;
    @Column

    private String director;

    private Long views = 0L;
    private Long bookings = 0L;

//    @NotBlank(message = "Trailer link is required")
//    @Pattern(regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$",
//            message = "Invalid YouTube URL format")
    private String trailer;

    @ElementCollection
    @CollectionTable(name = "movie_cast", joinColumns = @JoinColumn(name = "movie_id"))
    @Column
    private List<String>castMember;


//    @ManyToOne
//    @JoinColumn(name = "theater_id")
//    @JsonIgnore
//    private Theater theater;

    @ManyToMany

    @JoinTable(
            name = "movie_theater",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "theater_id")
    )
    @JsonIgnore
    private List<Theater> theaters;

}
