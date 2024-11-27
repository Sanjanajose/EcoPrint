package com.ecoprint.printmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private FirebaseMessaging firebaseMessaging;

    // Verify FirebaseMessaging bean initialization
    @GetMapping("/firebase")
    public String testFirebase() {
        return "FirebaseMessaging bean is initialized successfully!";
    }

    // Endpoint to send a test push notification
    @PostMapping("/push-notification")
    public ResponseEntity<String> sendTestNotification(
            @RequestParam String notificationToken,
            @RequestParam String title,
            @RequestParam String body) {
        try {
            // Build the notification message
            Message message = Message.builder()
                    .setToken(notificationToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            // Send the message using FirebaseMessaging
            String response = firebaseMessaging.send(message);
            return ResponseEntity.ok("Notification sent successfully! Response ID: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending notification: " + e.getMessage());
        }
    }
}
