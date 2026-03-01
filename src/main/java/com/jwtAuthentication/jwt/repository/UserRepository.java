package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);
    boolean existsByRole(Role role);
}
