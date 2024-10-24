package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.ActivityLog;
import com.ecoprint.printmanagement.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/activity")
    public List<ActivityLog> getAllActivityLogs() {
        return activityLogService.getAllLogs(); // Assuming you add getAllLogs() in ActivityLogService
    }
}

