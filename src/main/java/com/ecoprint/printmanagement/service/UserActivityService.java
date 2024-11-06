package com.ecoprint.printmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.UserActivity;
import com.ecoprint.printmanagement.model.User; 
import com.ecoprint.printmanagement.repository.UserActivityRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class UserActivityService {

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private UserRepository userRepository;

    public void logUserActivity(Long userId, String activityType, String activityDetail) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        UserActivity activity = new UserActivity();
        activity.setUser(user); 
        activity.setActivityType(activityType);
        activity.setActivityDetail(activityDetail);
        activity.setTimestamp(LocalDateTime.now());
        
        userActivityRepository.save(activity); 
    }

    
    
    // Updated method to fetch user activities for a specific user in a date range
     
    @Transactional
    public List<UserActivity> getUserActivitiesBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        List<UserActivity> activities = userActivityRepository.findActivitiesByUserAndDateRange(userId, start, end);
        activities.forEach(activity -> activity.getUser().getBackupCodes().size()); // Initialize backupCodes
        return activities;
    }

}
