package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Booking> findByRazorpayPaymentId(String razorpayPaymentId);

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByShowShowId(Long showId);

    List<Booking> findByPaymentStatusAndExpiresAtBefore(Booking.PaymentStatus paymentStatus, LocalDateTime now);

    // Count all confirmed bookings
    long countByPaymentStatus(Booking.PaymentStatus paymentStatus);

    // Count today's confirmed bookings (based on confirmation date)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.paymentStatus = :status AND b.confirmedAt >= :startOfDay")
    long countTodayBookings(@Param("status") Booking.PaymentStatus status, @Param("startOfDay") LocalDateTime startOfDay);

    // Calculate total revenue from confirmed bookings
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.paymentStatus = :status")
    double calculateTotalRevenue(@Param("status") Booking.PaymentStatus status);

    // Calculate today's revenue from confirmed bookings (based on confirmation date)
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.paymentStatus = :status AND b.confirmedAt >= :startOfDay")
    double calculateTodayRevenue(@Param("status") Booking.PaymentStatus status, @Param("startOfDay") LocalDateTime startOfDay);

    // Calculate total seats sold today
    @Query("SELECT COALESCE(SUM(b.numberOfSeats), 0) FROM Booking b WHERE b.paymentStatus = :status AND b.confirmedAt >= :startOfDay")
    long calculateSeatsSoldToday(@Param("status") Booking.PaymentStatus status, @Param("startOfDay") LocalDateTime startOfDay);

    // Get bookings between dates for chart
    @Query("SELECT b FROM Booking b WHERE b.paymentStatus = :status AND b.confirmedAt >= :startDate AND b.confirmedAt < :endDate ORDER BY b.confirmedAt")
    List<Booking> findBookingsBetweenDates(@Param("status") Booking.PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
