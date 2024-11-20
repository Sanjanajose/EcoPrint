package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.NotificationLog;
import com.ecoprint.printmanagement.repository.NotificationLogRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;


@Service
public abstract class PushNotificationService implements NotificationService {

    @Autowired
    private UserService userService; // To get device tokens

    
    @Autowired
    private NotificationLogRepository notificationLogRepository;
   
    @Override
    public void sendEmailNotification(String subject, String message) {
        // This method intentionally left blank for now as it's for push notifications
    }

    @Autowired
    private FirebaseMessaging firebaseMessaging;

    public void sendPushNotification(Long userId, String title, String message) {
        String token = getUserDeviceToken(userId); // Fetch user's device token

        if (token != null) {
            Message pushMessage = Message.builder()
                    .putData("title", title)
                    .putData("message", message)
                    .setToken(token)
                    .build();

            try {
                firebaseMessaging.send(pushMessage);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
        }
    }

    private String getUserDeviceToken(Long userId) {
        // Implement this logic to fetch the user's device token from your database
        return "userDeviceToken";
    }


    private void logNotification(String recipient, String message, String type) {
        // Log the notification
        NotificationLog log = new NotificationLog(recipient, message, type, LocalDateTime.now());
        // Assuming you have a NotificationLogRepository
        notificationLogRepository.save(log);
    }
}

