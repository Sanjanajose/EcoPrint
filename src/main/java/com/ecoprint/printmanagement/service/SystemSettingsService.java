package com.ecoprint.printmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.SystemSetting;
import com.ecoprint.printmanagement.repository.SystemSettingsRepository;

import java.util.Optional;

@Service
public class SystemSettingsService {

    @Autowired
    private SystemSettingsRepository systemSettingsRepository;

    /**
     * Retrieves the retention period for completed jobs.
     * Defaults to 30 days if no setting is found.
     *
     * @return the retention period in days
     */
    public int getRetentionPeriodForJobs() {
        return systemSettingsRepository.findByKey("retentionPeriod")
            .map(setting -> {
                try {
                    return Integer.parseInt(setting.getValue());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid value for retentionPeriod in system settings");
                }
            })
            .orElse(30); // Default retention period
    }

    /**
     * Retrieves the retention action (archive or delete).
     * Defaults to "delete" if no setting is found.
     *
     * @return the retention action
     */
    public String getRetentionAction() {
        return systemSettingsRepository.findByKey("retentionAction")
            .map(SystemSetting::getValue)
            .orElse("delete"); // Default retention action
    }

    /**
     * Updates or creates a system setting with the specified key and value.
     *
     * @param key   the setting key
     * @param value the new value for the setting
     */
    public void updateSetting(String key, String value) {
        Optional<SystemSetting> existingSetting = systemSettingsRepository.findByKey(key);

        SystemSetting setting = existingSetting.orElseGet(() -> new SystemSetting(key, value));
        setting.setValue(value);
        systemSettingsRepository.save(setting);
    }
}
