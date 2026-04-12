package com.jwtAuthentication.jwt.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event_tickets")
public class EventTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", unique = true, nullable = false)
    private String ticketId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private EventBooking booking;

    @Column(name = "qr_code", length = 1024)
    private String qrCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
        if (ticketId == null || ticketId.isBlank()) {
            ticketId = "EVTKT" + System.currentTimeMillis();
        }
    }
}
