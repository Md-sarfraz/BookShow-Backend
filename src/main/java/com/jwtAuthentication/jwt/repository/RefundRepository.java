package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
	Optional<Refund> findTopByBookingBookingIdOrderByCreatedAtDesc(Long bookingId);
	List<Refund> findByBookingBookingIdOrderByCreatedAtDesc(Long bookingId);

	@Query("""
			SELECT r
			FROM Refund r
			JOIN FETCH r.booking b
			LEFT JOIN FETCH b.show s
			LEFT JOIN FETCH s.movie
			LEFT JOIN FETCH s.theater
			WHERE b.userId = :userId
			ORDER BY r.createdAt DESC
			""")
	List<Refund> findUserRefundHistory(@Param("userId") Long userId);
}
