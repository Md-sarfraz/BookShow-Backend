package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {

    Optional<City> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
