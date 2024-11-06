package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.model.ActivityLog;
import com.ecoprint.printmanagement.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Method to log a generic user action
    public void logAction(String action, String username, Long userId, String description) {
        ActivityLog log = new ActivityLog(
            action,
            username,
            LocalDateTime.now(),
            description,
            userId,
            "User action: " + description
        );
        activityLogRepository.save(log);  // Persist log to the database
        System.out.println("Action logged: " + action + " by " + username);
    }

    // Method to log changes in user roles
    public void logRoleChange(Long userId, String roleNames, String action) {
        ActivityLog log = new ActivityLog(
            action,
            "System Admin",  // Replace with the actual username of the Admin/Super Admin
            LocalDateTime.now(),
            "Assigned roles: " + roleNames + " to user with ID: " + userId,
            userId,
            "Role change action"
        );
        activityLogRepository.save(log);  // Persist log to the database
        System.out.println("Role change logged: " + action + " for User ID: " + userId);
    }

    // Method to fetch all activity logs
    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAll();
    }
}
