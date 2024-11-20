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
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;

	    @Column(name = "job_completed")
	    private boolean jobCompletedNotificationEnabled;

	    @Column(name = "job_failed")
	    private boolean jobFailedNotificationEnabled;

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
	


}
