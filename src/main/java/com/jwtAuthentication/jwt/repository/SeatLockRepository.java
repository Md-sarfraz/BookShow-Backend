package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatLockRepository extends JpaRepository<SeatLock, Long> {

    /** Active (non-expired) locks for specific seat labels in a show */
    @Query("SELECT sl FROM SeatLock sl WHERE sl.show.showId = :showId AND sl.seatLabel IN :seatLabels AND sl.lockedUntil > :now")
    List<SeatLock> findActiveLocksForSeats(
            @Param("showId") Long showId,
            @Param("seatLabels") List<String> seatLabels,
            @Param("now") LocalDateTime now);

    /** All active (non-expired) locks for a show — used by seat-status API */
    @Query("SELECT sl FROM SeatLock sl WHERE sl.show.showId = :showId AND sl.lockedUntil > :now")
    List<SeatLock> findActiveLocksForShow(
            @Param("showId") Long showId,
            @Param("now") LocalDateTime now);

    /** Delete all locks associated with a Razorpay order (payment success or failure) */
    @Modifying
    @Query("DELETE FROM SeatLock sl WHERE sl.razorpayOrderId = :razorpayOrderId")
    void deleteByRazorpayOrderId(@Param("razorpayOrderId") String razorpayOrderId);

    /** Delete all expired locks — called by scheduled cleanup task */
    @Modifying
    @Query("DELETE FROM SeatLock sl WHERE sl.lockedUntil < :now")
    void deleteExpiredLocks(@Param("now") LocalDateTime now);
}
