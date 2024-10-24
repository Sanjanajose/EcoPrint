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

    public void logAction(String action, String username, String details, Long userId, String description) {
        // Create ActivityLog object with all parameters
        ActivityLog log = new ActivityLog(action, username, LocalDateTime.now(), details, userId, description);
        
        // Save the log to the repository (and eventually to the database)
        activityLogRepository.save(log);
    }

// Add getAllLogs method to fetch all activity logs
    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAll();  // Retrieves all logs from the database
    }
}
