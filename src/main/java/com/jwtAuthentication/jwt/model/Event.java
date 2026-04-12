package com.jwtAuthentication.jwt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_title_date_location", columnNames = {"title", "date", "location"})
})
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

    @Column(name = "total_tickets", nullable = false)
    private Integer totalTickets = 200;

    @PrePersist
    @PreUpdate
    private void ensureTotalTickets() {
        if (totalTickets == null || totalTickets <= 0) {
            totalTickets = 200;
        }
    }
}
