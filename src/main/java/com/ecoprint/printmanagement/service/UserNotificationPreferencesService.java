package com.ecoprint.printmanagement.service;
import com.ecoprint.printmanagement.model.UserNotificationPreferences;
import com.ecoprint.printmanagement.repository.UserNotificationPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationPreferencesService {
	

	    @Autowired
	    private UserNotificationPreferencesRepository userNotificationPreferencesRepository;

	    public UserNotificationPreferences getUserPreferences(Long userId) {
	        return userNotificationPreferencesRepository.findByUserId(userId);
	    }

	    public void updateUserPreferences(Long userId, UserNotificationPreferences preferences) {
	        UserNotificationPreferences existingPreferences = getUserPreferences(userId);
	        if (existingPreferences != null) {
	            existingPreferences.setJobCompletedNotificationEnabled(preferences.isJobCompletedNotificationEnabled());
	            existingPreferences.setJobFailedNotificationEnabled(preferences.isJobFailedNotificationEnabled());
	            userNotificationPreferencesRepository.save(existingPreferences);
	        }
	    }
	}



