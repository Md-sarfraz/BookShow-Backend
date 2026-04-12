package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.EventTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventTicketRepository extends JpaRepository<EventTicket, Long> {
    Optional<EventTicket> findByBookingBookingId(Long bookingId);

    Optional<EventTicket> findByBookingBookingReference(String bookingReference);
}
