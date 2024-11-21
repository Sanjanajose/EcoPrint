package com.ecoprint.printmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.UserNotificationPreferences;
import com.ecoprint.printmanagement.model.UserNotificationPreferencesDTO;
import com.ecoprint.printmanagement.service.NotificationService;
import com.ecoprint.printmanagement.service.UserNotificationPreferencesService;
import com.ecoprint.printmanagement.service.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {


		@Autowired
		@Qualifier("emailNotificationService") // Specify the bean to use
		private NotificationService emailNotificationService;

		@Autowired
		@Qualifier("pushNotificationService") // Specify the bean to use
		private NotificationService pushNotificationService;

	    @Autowired
	    UserService userService;
	    
	    

	    @Autowired
	    private UserNotificationPreferencesService userNotificationPreferencesService;

	    @GetMapping("/preferences/{userId}")
	    public ResponseEntity<UserNotificationPreferences> getPreferences(@PathVariable Long userId) {
	        UserNotificationPreferences preferences = userNotificationPreferencesService.getUserPreferences(userId)
	                .orElseThrow(() -> new ResourceNotFoundException("UserNotificationPreferences", "userId", userId));
	        return ResponseEntity.ok(preferences);
	    }


	   
	    
	   /* @PutMapping("/preferences/{userId}")
	    public ResponseEntity<String> updatePreferences(
	            @PathVariable Long userId,
	            @RequestBody UserNotificationPreferencesDTO preferencesDTO) {
	        // Fetch the user to ensure they exist
	        User user = userService.findById(userId)
	                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

	        // Fetch the existing preferences or create new ones if not found
	        UserNotificationPreferences preferences = userNotificationPreferencesService.findByUserId(userId)
	                .orElseGet(() -> {
	                    UserNotificationPreferences newPreferences = new UserNotificationPreferences();
	                    newPreferences.setUser(user);
	                    return newPreferences;
	                });

	        // Update the fields
	        preferences.setJobCompletedNotificationEnabled(preferencesDTO.isJobCompletedNotificationEnabled());
	        preferences.setJobFailedNotificationEnabled(preferencesDTO.isJobFailedNotificationEnabled());

	        // Save the preferences (either updated or newly created)
	        userNotificationPreferencesService.save(preferences);

	        return ResponseEntity.ok("Notification preferences updated successfully.");
	    }*/
	    
	    
	    
	    @PutMapping("/preferences/{userId}")
	    public ResponseEntity<String> updatePreferences(
	            @PathVariable Long userId,
	            @RequestBody UserNotificationPreferencesDTO preferencesDTO) {
	        // Fetch the user to ensure they exist
	        User user = userService.findById(userId)
	                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

	        // Fetch the existing preferences or create new ones if not found
	        UserNotificationPreferences preferences = userNotificationPreferencesService.findByUserId(userId)
	                .orElseGet(() -> {
	                    UserNotificationPreferences newPreferences = new UserNotificationPreferences();
	                    newPreferences.setUser(user);
	                    return newPreferences;
	                });

	        // Update the fields
	        preferences.setJobCompletedNotificationEnabled(preferencesDTO.isJobCompletedNotificationEnabled());
	        preferences.setJobFailedNotificationEnabled(preferencesDTO.isJobFailedNotificationEnabled());
	        preferences.setPreferInApp(preferencesDTO.isPreferInApp());
	        preferences.setPreferEmail(preferencesDTO.isPreferEmail());

	        // Save the preferences
	        userNotificationPreferencesService.save(preferences);

	        return ResponseEntity.ok("Notification preferences updated successfully.");
	    }



	}


