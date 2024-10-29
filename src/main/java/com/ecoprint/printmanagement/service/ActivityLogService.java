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
        // Logic for saving a new ActivityLog with the given details
        System.out.println("Action: " + action + ", User: " + username + ", User ID: " + userId + ", Description: " + description);
        // Actual implementation should save to the database.
    }

    // Method to log changes in user roles
    public void logRoleChange(Long userId, String roleName, String action) {
        // Logic for saving a role change ActivityLog with the given details
        System.out.println("Role Change Action: " + action + ", Role: " + roleName + ", User ID: " + userId);
        // Actual implementation should save to the database.
    }

// Add getAllLogs method to fetch all activity logs
    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAll();  // Retrieves all logs from the database
    }
}
