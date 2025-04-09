package com.jwtAuthentication.jwt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String title;

    @Column
    private String category;

    @Column
    private String date;

    @Column
    private String time;

    @Column
    private String location;

    @Column
    private String price;

    @Column
    private String imageUrl;

    @Column
    private String backgroundImageUrl;

    @Column(columnDefinition = "TEXT") // In case description is long
    private String description;
}
