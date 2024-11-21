package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.NotificationLog;
import com.ecoprint.printmanagement.model.UserNotificationPreferences;
import com.ecoprint.printmanagement.repository.NotificationLogRepository;
import com.ecoprint.printmanagement.repository.UserNotificationPreferencesRepository;

@Service("emailNotificationService")
public class EmailNotificationService implements NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NotificationLogRepository notificationLogRepository;
    
    @Autowired
    UserNotificationPreferencesRepository userNotificationPreferencesRepository;

    @Override
    public void sendEmailNotification(String recipient, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(recipient);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailSender.send(mailMessage);

        logNotification(recipient, message, "EMAIL");
    }

    @Override
    public void sendPushNotification(String recipientId, String title, String message) {
        throw new UnsupportedOperationException("Push notifications are not supported by EmailNotificationService.");
    }

    @Override
    public void sendNotificationBasedOnPreferences(UserNotificationPreferences preferences, String title, String message) {
        if (preferences.isPreferEmail()) {
            sendEmailNotification(preferences.getUser().getEmail(), title, message);
        }
    }
    
    
    


    private void logNotification(String recipient, String message, String type) {
        NotificationLog log = new NotificationLog(recipient, message, type, LocalDateTime.now());
        notificationLogRepository.save(log);
    }
    
}
