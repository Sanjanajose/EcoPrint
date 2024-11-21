package com.ecoprint.printmanagement.model;

public class UserNotificationPreferencesDTO {

    private boolean jobCompletedNotificationEnabled;
    private boolean jobFailedNotificationEnabled;
    private boolean preferInApp; // Field for in-app alerts preference
    private boolean preferEmail; // Field for email notifications preference

    // Getters and setters
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
