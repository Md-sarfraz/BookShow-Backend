package com.jwtAuthentication.jwt.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shows", uniqueConstraints = {
        @UniqueConstraint(name = "uk_show_movie_theater_date_time_screen",
                columnNames = {"movie_id", "theater_id", "showDate", "showTime", "screenNumber"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long showId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "theaters"})
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "movies"})
    private Theater theater;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate showDate;

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime showTime;

    @Column
    private Double price;

    @Column
    private Integer availableSeats;

    @Column
    private Integer totalSeats;

    @Column
    private String language;

    @Column
    private String format; // 2D, 3D, IMAX, etc.

    @Column
    private String screenNumber; // Screen number within the theater (e.g., "Screen 1", "Screen 2")

    // Helper method to check if show is available
    public boolean isAvailable() {
        return availableSeats != null && availableSeats > 0;
    }
}
