package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {
    List<EventBooking> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<EventBooking> findByBookingReference(String bookingReference);

    Optional<EventBooking> findByRazorpayOrderId(String razorpayOrderId);

    List<EventBooking> findByBookingStatusAndLockExpiresAtBefore(EventBooking.BookingStatus bookingStatus,
                                                                 LocalDateTime lockExpiresAt);

    @Query("SELECT COALESCE(SUM(eb.numberOfTickets), 0) FROM EventBooking eb WHERE eb.event.id = :eventId AND eb.bookingStatus = :status")
    long sumTicketsByEventIdAndStatus(@Param("eventId") Integer eventId,
                                      @Param("status") EventBooking.BookingStatus status);

    @Query("SELECT COALESCE(SUM(eb.numberOfTickets), 0) FROM EventBooking eb WHERE eb.event.id = :eventId AND eb.bookingStatus = 'LOCKED' AND eb.lockExpiresAt > :now")
    long sumActiveLockedTicketsByEventId(@Param("eventId") Integer eventId,
                                         @Param("now") LocalDateTime now);
}
