package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.NotificationLog;
import com.ecoprint.printmanagement.repository.NotificationLogRepository;

@Service
public class PushNotificationService implements NotificationService {

    @Autowired
    private UserService userService; // To get device tokens

    
    @Autowired
    private NotificationLogRepository notificationLogRepository;
    @Override
    public void sendEmailNotification(String subject, String message) {
        // Not implemented for push notifications
    }

    @Override
    public void sendPushNotification(String title, String message) {
        // Retrieve notification tokens for admins
        List<String> adminNotificationTokens = userService.getAdminNotificationTokens();

        for (String token : adminNotificationTokens) {
            // Build and send push notification using Firebase or another service
            // Example of sending a push notification using Firebase Cloud Messaging (placeholder code):
            // FirebaseMessaging.getInstance().sendAsync(
            //     Message.builder()
            //            .setToken(token)
            //            .putData("title", title)
            //            .putData("message", message)
            //            .build()
            // );

            // Log the push notification
            logNotification(token, message, "PUSH");
        }
    }


    private void logNotification(String recipient, String message, String type) {
        // Log the notification
        NotificationLog log = new NotificationLog(recipient, message, type, LocalDateTime.now());
        // Assuming you have a NotificationLogRepository
        notificationLogRepository.save(log);
    }
}