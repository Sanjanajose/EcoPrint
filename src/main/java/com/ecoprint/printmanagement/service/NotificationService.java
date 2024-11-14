package com.ecoprint.printmanagement.service;

public interface NotificationService {
	void sendEmailNotification(String subject, String message);
    void sendPushNotification(String title, String message);
}


