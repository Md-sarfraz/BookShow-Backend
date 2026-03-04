package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByShowShowId(Long showId);
}
