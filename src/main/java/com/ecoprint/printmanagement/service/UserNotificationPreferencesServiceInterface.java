package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.model.UserNotificationPreferences;

import java.util.Optional;

public interface UserNotificationPreferencesServiceInterface {

    Optional<UserNotificationPreferences> findByUserId(Long userId);

    void save(UserNotificationPreferences preferences);

    Optional<UserNotificationPreferences> getUserPreferences(Long userId);

    Optional<UserNotificationPreferences> findByUserEmail(String email);
}
