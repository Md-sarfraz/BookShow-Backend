package com.jwtAuthentication.jwt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @Column
    private String name;

    @ElementCollection
    @CollectionTable(name = "movie_times", joinColumns = @JoinColumn(name = "movieId"))
    @Column(name = "time")
    private List<String> time;

    @Column
    private String location;
    @Column
    private String city;
    @Column
    private String state;
    @Column
    private String postalCode;
    @Column
    private String contactNo;
    @Column
    private Integer capacity;
    @Column
    private Integer screens;
    @Column
    private String operatingHours;

//    @Column
//    @OneToMany(mappedBy = "theater")
//    private List<Movie>ListOfMovies;


    @ManyToMany(mappedBy = "theaters")
    @JsonIgnoreProperties("theaters")
    private List<Movie> movies;


}
