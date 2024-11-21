package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.model.UserNotificationPreferences;
import com.ecoprint.printmanagement.repository.UserNotificationPreferencesRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationPreferencesService implements UserNotificationPreferencesServiceInterface {

    @Autowired
    private UserNotificationPreferencesRepository preferencesRepository;

    @Override
    public Optional<UserNotificationPreferences> findByUserId(Long userId) {
        return preferencesRepository.findByUserId(userId);
    }

    @Override
    public void save(UserNotificationPreferences preferences) {
        preferencesRepository.save(preferences);
    }

    @Override
    public Optional<UserNotificationPreferences> getUserPreferences(Long userId) {
        return preferencesRepository.findByUserId(userId);
    }

    @Override
    public Optional<UserNotificationPreferences> findByUserEmail(String email) {
        return preferencesRepository.findByUserEmail(email);
    }
}
