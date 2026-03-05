package com.jwtAuthentication.jwt.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "entity_name")
    private String entityName; // Name of the user, movie, theater, etc.

    @Column(name = "entity_id")
    private Long entityId; // ID of the related entity

    @Column(name = "additional_info")
    private String additionalInfo; // Extra details like location, screen, etc.

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ActivityType {
        USER_REGISTERED,
        MOVIE_ADDED,
        MOVIE_UPDATED,
        SHOW_CREATED,
        THEATER_ADDED,
        THEATER_UPDATED,
        BOOKING_CONFIRMED,
        EVENT_ADDED
    }
}
