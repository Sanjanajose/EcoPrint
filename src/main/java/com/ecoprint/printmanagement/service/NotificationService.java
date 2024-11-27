package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.model.UserNotificationPreferences;

public interface NotificationService {

	void sendEmailNotification(String recipient, String subject, String message);
    void sendPushNotification(Long recipientId, String title, String message);
    void sendNotificationBasedOnPreferences(UserNotificationPreferences preferences, String title, String message);
}



