package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.ActivityLog;
import com.ecoprint.printmanagement.service.ActivityLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/activity")
    public List<ActivityLog> getAllActivityLogs() {
        return activityLogService.getAllLogs(); // Ensure getAllLogs() is implemented in ActivityLogService
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ActivityLog>> getActivitiesByUser(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLog> logs = activityLogService.getActivitiesByUser(userId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/range")
    public ResponseEntity<List<ActivityLog>> getActivitiesWithinTimeRange(
        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<ActivityLog> logs = activityLogService.getActivitiesWithinTimeRange(start, end);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/metrics/{userId}")
    public ResponseEntity<Map<String, Long>> getProductivityMetrics(@PathVariable Long userId) {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("totalPrintJobsHandled", activityLogService.getTotalPrintJobsHandled(userId));
        return ResponseEntity.ok(metrics);
    }
}
