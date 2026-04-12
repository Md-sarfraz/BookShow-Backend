package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event,Integer> {

    // Duplicate check: same title + date + location
    boolean existsByTitleIgnoreCaseAndDateAndLocationIgnoreCase(String title, String date, String location);

    boolean existsByTitleIgnoreCaseAndDateAndLocationIgnoreCaseAndIdNot(
            String title,
            String date,
            String location,
            int id
    );
}
