package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.isRead = false")
    int markAllAsRead();
}
