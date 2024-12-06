package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.service.SystemSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    @Autowired
    private SystemSettingsService systemSettingsService;

    @PutMapping("/retention-action")
    public String updateRetentionAction(@RequestParam String action) {
        if (!"archive".equalsIgnoreCase(action) && !"delete".equalsIgnoreCase(action)) {
            return "Invalid action. Choose 'archive' or 'delete'.";
        }
        systemSettingsService.updateSetting("job_retention_action", action.toLowerCase());
        return "Retention action updated to " + action.toLowerCase() + ".";
    }

    @PutMapping("/retention-period")
    public String updateRetentionPeriod(@RequestParam int days) {
        if (days <= 0) {
            return "Retention period must be positive.";
        }
        systemSettingsService.updateSetting("job_retention_days", String.valueOf(days));
        return "Retention period updated to " + days + " days.";
    }
}
