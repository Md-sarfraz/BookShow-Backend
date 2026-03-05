package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Activity;
import com.jwtAuthentication.jwt.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    // Log a new activity
    @Transactional
    public Activity logActivity(Activity.ActivityType type, String message, String entityName, Long entityId, String additionalInfo) {
        Activity activity = new Activity();
        activity.setActivityType(type);
        activity.setMessage(message);
        activity.setEntityName(entityName);
        activity.setEntityId(entityId);
        activity.setAdditionalInfo(additionalInfo);
        return activityRepository.save(activity);
    }

    // Get recent activities (limit to last 10)
    public List<Activity> getRecentActivities(int limit) {
        List<Activity> activities = activityRepository.findRecentActivities();
        return activities.stream().limit(limit).toList();
    }

    // Get all activities
    public List<Activity> getAllActivities() {
        return activityRepository.findRecentActivities();
    }
}
