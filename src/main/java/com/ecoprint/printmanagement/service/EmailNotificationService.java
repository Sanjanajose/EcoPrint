package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.NotificationLog;
import com.ecoprint.printmanagement.repository.NotificationLogRepository;

@Service
public class EmailNotificationService implements NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService; // Service to get admin emails
    
    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Override
    public void sendEmailNotification(String subject, String message) {
        // Get list of admin emails
        List<String> adminEmails = userService.getAdminEmails();

        for (String email : adminEmails) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailSender.send(mailMessage);

            // Log the notification
            logNotification(email, message, "EMAIL");
        }
    }

    @Override
    public void sendPushNotification(String title, String message) {
        // Not implemented here, but can be implemented later
    }

    private void logNotification(String recipient, String message, String type) {
        // Log the notification
        NotificationLog log = new NotificationLog(recipient, message, type, LocalDateTime.now());
        // Assuming you have a NotificationLogRepository
        notificationLogRepository.save(log);
    }
}