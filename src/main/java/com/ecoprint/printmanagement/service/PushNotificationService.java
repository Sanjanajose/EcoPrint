package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.List;
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
    public void sendPushNotification(Long recipientId, String title, String message) {
        // Fetch the device tokens of the user
        List<UserDevice> userDevices = userDeviceRepository.findByUserId(recipientId);

        if (userDevices != null && !userDevices.isEmpty()) {
            for (UserDevice device : userDevices) {
                if (device.getNotificationToken() != null) {
                    String token = device.getNotificationToken();

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
                        System.err.println("Error sending push notification to token " + token + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("No valid device token found for one of the devices for userId: " + recipientId);
                }
            }
        } else {
            System.err.println("No devices found for userId: " + recipientId);
        }
    }

    @Override
    public void sendNotificationBasedOnPreferences(UserNotificationPreferences preferences, String title, String message) {
        if (preferences.isPreferInApp() && preferences.getUser() != null) {
            sendPushNotification(preferences.getUser().getId(), title, message); // Pass Long directly
        } else {
            System.err.println("Notification preferences are not set for in-app notifications or user is null.");
        }
    }

    private void logNotification(String recipient, String message, String type) {
        NotificationLog log = new NotificationLog(recipient, message, type, LocalDateTime.now());
        notificationLogRepository.save(log);
    }
}
