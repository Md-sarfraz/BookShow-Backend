package com.jwtAuthentication.jwt.repository;

import com.jwtAuthentication.jwt.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Get recent activities, ordered by most recent first
    @Query("SELECT a FROM Activity a ORDER BY a.createdAt DESC")
    List<Activity> findRecentActivities();

    // Get activities by type
    List<Activity> findByActivityTypeOrderByCreatedAtDesc(Activity.ActivityType activityType);
}
