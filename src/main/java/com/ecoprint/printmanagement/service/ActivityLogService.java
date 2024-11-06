package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.model.ActivityLog;
import com.ecoprint.printmanagement.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Autowired
    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    // Unified logging method for all user actions
    public void logActivity(String action, String username, Long userId, String description) {
        ActivityLog log = new ActivityLog(
            action,
            username,
            LocalDateTime.now(),
            description,
            userId,
            action + " action: " + description
        );
        activityLogRepository.save(log);
        System.out.println("Action logged: " + action + " by " + username);
    }

    // Dedicated method to log role changes
    public void logRoleChange(Long userId, String roles, String action) {
        String description = "Roles: " + roles + " were " + action + " for user with ID: " + userId;
        logActivity(action, "System Admin", userId, description);  // Log as "System Admin" or actual admin's username
        System.out.println("Role change logged: " + action + " for User ID: " + userId);
    }

    // Fetch all activity logs
    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAll();
    }

    // Paginated activity logs by user
    public Page<ActivityLog> getActivitiesByUser(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserId(userId, pageable);
    }

    // Activity logs within a specified time range
    public List<ActivityLog> getActivitiesWithinTimeRange(LocalDateTime start, LocalDateTime end) {
        return activityLogRepository.findByTimestampBetween(start, end);
    }

    // Specific metric for total print jobs handled by a user
    public long getTotalPrintJobsHandled(Long userId) {
        return activityLogRepository.countByUserIdAndAction(userId, "PRINT_JOB");
    }
}
