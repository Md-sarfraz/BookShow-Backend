package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TheaterRepository extends JpaRepository<Theater,Integer> {

}
