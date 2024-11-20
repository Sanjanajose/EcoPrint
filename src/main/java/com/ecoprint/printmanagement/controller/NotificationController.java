package com.ecoprint.printmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.model.UserNotificationPreferences;
import com.ecoprint.printmanagement.service.NotificationService;
import com.ecoprint.printmanagement.service.UserNotificationPreferencesService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {


	    @Autowired
	    private NotificationService notificationService;

	    @Autowired
	    private UserNotificationPreferencesService userNotificationPreferencesService;

	    @GetMapping("/preferences/{userId}")
	    public ResponseEntity<UserNotificationPreferences> getPreferences(@PathVariable Long userId) {
	        UserNotificationPreferences preferences = userNotificationPreferencesService.getUserPreferences(userId);
	        return ResponseEntity.ok(preferences);
	    }

	    @PutMapping("/preferences/{userId}")
	    public ResponseEntity<String> updatePreferences(@PathVariable Long userId, @RequestBody UserNotificationPreferences preferences) {
	        userNotificationPreferencesService.updateUserPreferences(userId, preferences);
	        return ResponseEntity.ok("Notification preferences updated successfully.");
	    }
	}


