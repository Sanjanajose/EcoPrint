package com.ecoprint.printmanagement.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecoprint.printmanagement.model.PerformanceMetric; 
import com.ecoprint.printmanagement.model.UserActivity; 
import com.ecoprint.printmanagement.service.PerformanceMetricService;
import com.ecoprint.printmanagement.service.UserActivityService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    
    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private PerformanceMetricService performanceMetricService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/activity/{userId}")
    @Operation(summary = "allows ADMIN to get the list of activities between a time frame for a user ")
    public ResponseEntity<List<UserActivity>> getUserActivities(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        try {
            List<UserActivity> activities = userActivityService.getUserActivitiesBetween(userId, start, end);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User activities not found", e);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/performance/{userId}")
    @Operation(summary = "allows ADMIN to get the performance metrics of a user between a timeframe ")
    public List<PerformanceMetric> getUserPerformance(
            @PathVariable Long userId, 
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, 
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return performanceMetricService.getPerformanceMetrics(userId, start, end);
    }
}
