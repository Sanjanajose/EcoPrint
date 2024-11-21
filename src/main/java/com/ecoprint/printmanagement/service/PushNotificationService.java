package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.NotificationLog;
import com.ecoprint.printmanagement.model.UserDevice;
import com.ecoprint.printmanagement.model.UserNotificationPreferences;
import com.ecoprint.printmanagement.repository.NotificationLogRepository;
import com.ecoprint.printmanagement.repository.UserDeviceRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

@Service("pushNotificationService")
public class PushNotificationService implements NotificationService {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private FirebaseMessaging firebaseMessaging;

    @Override
    public void sendEmailNotification(String recipient, String subject, String message) {
        throw new UnsupportedOperationException("Email notifications are not supported by PushNotificationService.");
    }

    @Override
    public void sendPushNotification(String recipientId, String title, String message) {
        // Fetch the device token of the user
        Optional<UserDevice> userDeviceOpt = userDeviceRepository.findByUserId(Long.parseLong(recipientId));
        
        if (userDeviceOpt.isPresent() && userDeviceOpt.get().getNotificationToken() != null) {
            String token = userDeviceOpt.get().getNotificationToken();

            // Build the push notification message
            Message pushMessage = Message.builder()
                    .putData("title", title)
                    .putData("message", message)
                    .setToken(token)
                    .build();

            try {
                // Send the push notification via Firebase
                firebaseMessaging.send(pushMessage);
                logNotification(token, message, "PUSH");
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No valid device token found for userId: " + recipientId);
        }
    }

    @Override
    public void sendNotificationBasedOnPreferences(UserNotificationPreferences preferences, String title, String message) {
        if (preferences.isPreferInApp()) {
            sendPushNotification(preferences.getUser().getId().toString(), title, message);
        }
    }

    private void logNotification(String recipient, String message, String type) {
        NotificationLog log = new NotificationLog(recipient, message, type, LocalDateTime.now());
        notificationLogRepository.save(log);
    }
}
