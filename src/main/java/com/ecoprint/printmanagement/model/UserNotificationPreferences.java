package com.ecoprint.printmanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_notification_preferences")
public class UserNotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "job_completed",nullable = true)
    private boolean jobCompletedNotificationEnabled= true; // Default value


    @Column(name = "job_failed",nullable = true)
    private boolean jobFailedNotificationEnabled = true;

    @Column(name = "prefer_in_app", nullable = true)
    private boolean preferInApp =true; // Added field for in-app alerts preference

    @Column(name = "prefer_email", nullable = true)
    private boolean preferEmail=true; // Added field for email notifications preference

    // Getters and setters

    public Long getId() {
        return id;
    }
    

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isJobCompletedNotificationEnabled() {
        return jobCompletedNotificationEnabled;
    }

    public void setJobCompletedNotificationEnabled(boolean jobCompletedNotificationEnabled) {
        this.jobCompletedNotificationEnabled = jobCompletedNotificationEnabled;
    }

    public boolean isJobFailedNotificationEnabled() {
        return jobFailedNotificationEnabled;
    }

    public void setJobFailedNotificationEnabled(boolean jobFailedNotificationEnabled) {
        this.jobFailedNotificationEnabled = jobFailedNotificationEnabled;
    }

    public boolean isPreferInApp() {
        return preferInApp;
    }

    public void setPreferInApp(boolean preferInApp) {
        this.preferInApp = preferInApp;
    }

    public boolean isPreferEmail() {
        return preferEmail;
    }

    public void setPreferEmail(boolean preferEmail) {
        this.preferEmail = preferEmail;
    }
}
