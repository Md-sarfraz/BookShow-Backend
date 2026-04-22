package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Notification;
import com.jwtAuthentication.jwt.model.NotificationType;
import com.jwtAuthentication.jwt.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final String TOPIC_ADMIN = "/topic/admin";
    private static final String TOPIC_NEW_NOTIFICATION = "/topic/new_notification";

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JdbcTemplate jdbcTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate,
                               JdbcTemplate jdbcTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    private void ensureNotificationsTableExists() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    message VARCHAR(500) NOT NULL,
                    type VARCHAR(30) NOT NULL,
                    created_at DATETIME NOT NULL,
                    is_read BIT(1) NOT NULL DEFAULT b'0',
                    PRIMARY KEY (id)
                )
                """);
    }

    private Notification mapNotification(org.springframework.jdbc.support.rowset.SqlRowSet rs) {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setMessage(rs.getString("message"));

        String rawType = rs.getString("type");
        try {
            n.setType(NotificationType.valueOf(rawType));
        } catch (Exception ex) {
            n.setType(NotificationType.SYSTEM);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        n.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now());
        n.setIsRead(rs.getBoolean("is_read"));
        return n;
    }

    private Map<String, Object> toSocketPayload(Notification notification) {
        boolean isRead = Boolean.TRUE.equals(notification.getIsRead());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "new_notification");
        payload.put("id", notification.getId() == null ? null : String.valueOf(notification.getId()));
        payload.put("message", notification.getMessage());
        payload.put("isRead", isRead);
        payload.put("createdAt", notification.getCreatedAt() == null ? LocalDateTime.now().toString() : notification.getCreatedAt().toString());
        return payload;
    }

    @Transactional
    public Notification createAndBroadcast(String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        Notification payload = notification;
        try {
            payload = notificationRepository.save(notification);
        } catch (Exception ex) {
            logger.error("Failed to persist notification via JPA, trying JDBC fallback.", ex);
            try {
                ensureNotificationsTableExists();
                jdbcTemplate.update(
                        "INSERT INTO notifications (message, type, created_at, is_read) VALUES (?, ?, ?, ?)",
                        notification.getMessage(),
                        notification.getType().name(),
                        Timestamp.valueOf(notification.getCreatedAt()),
                        false
                );

                org.springframework.jdbc.support.rowset.SqlRowSet rs = jdbcTemplate.queryForRowSet(
                        "SELECT id, message, type, created_at, is_read FROM notifications ORDER BY id DESC LIMIT 1"
                );
                if (rs.next()) {
                    payload = mapNotification(rs);
                }
            } catch (Exception fallbackEx) {
                // Keep registration/booking flows alive even if notification table has issues.
                logger.error("JDBC fallback persistence for notification also failed.", fallbackEx);
            }
        }

        try {
            // Keep legacy topic for existing subscribers.
            messagingTemplate.convertAndSend(TOPIC_ADMIN, payload);
            // Primary event topic consumed by the frontend real-time listener.
            messagingTemplate.convertAndSend(TOPIC_NEW_NOTIFICATION, toSocketPayload(payload));
        } catch (Exception ex) {
            logger.error("Failed to broadcast notification over websocket.", ex);
        }

        return payload;
    }

    public List<Notification> getAllNotifications() {
        try {
            return notificationRepository.findAllByOrderByCreatedAtDesc();
        } catch (Exception ex) {
            logger.error("Failed to load notifications via JPA, trying JDBC fallback.", ex);
            try {
                ensureNotificationsTableExists();
                return jdbcTemplate.query(
                        "SELECT id, message, type, created_at, is_read FROM notifications ORDER BY created_at DESC",
                        (resultSet, rowNum) -> {
                            Notification n = new Notification();
                            n.setId(resultSet.getLong("id"));
                            n.setMessage(resultSet.getString("message"));

                            String rawType = resultSet.getString("type");
                            try {
                                n.setType(NotificationType.valueOf(rawType));
                            } catch (Exception ignored) {
                                n.setType(NotificationType.SYSTEM);
                            }

                            Timestamp createdAt = resultSet.getTimestamp("created_at");
                            n.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now());
                            n.setIsRead(resultSet.getBoolean("is_read"));
                            return n;
                        }
                );
            } catch (Exception fallbackEx) {
                logger.error("JDBC fallback read for notifications failed.", fallbackEx);
                return List.of();
            }
        }
    }

    @Transactional
    public Notification markAsRead(Long id) {
        try {
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

            notification.setIsRead(true);
            return notificationRepository.save(notification);
        } catch (Exception ex) {
            logger.error("Failed to update notification via JPA, trying JDBC fallback.", ex);
            ensureNotificationsTableExists();
            int updated = jdbcTemplate.update("UPDATE notifications SET is_read = ? WHERE id = ?", true, id);
            if (updated == 0) {
                throw new RuntimeException("Notification not found with id: " + id);
            }

            org.springframework.jdbc.support.rowset.SqlRowSet rs = jdbcTemplate.queryForRowSet(
                    "SELECT id, message, type, created_at, is_read FROM notifications WHERE id = ?",
                    id
            );
            if (rs.next()) {
                return mapNotification(rs);
            }
            throw new RuntimeException("Notification not found with id: " + id);
        }
    }

    @Transactional
    public int markAllAsRead() {
        try {
            return notificationRepository.markAllAsRead();
        } catch (Exception ex) {
            logger.error("Failed to mark all notifications as read via JPA, trying JDBC fallback.", ex);
            ensureNotificationsTableExists();
            return jdbcTemplate.update("UPDATE notifications SET is_read = ? WHERE is_read = ?", true, false);
        }
    }
}
