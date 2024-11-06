package com.ecoprint.printmanagement.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecoprint.printmanagement.model.UserActivity;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.service.PerformanceMetricService;
import com.ecoprint.printmanagement.repository.UserActivityRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MetricsScheduler {

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private PerformanceMetricService performanceMetricService;

    @Autowired
    private UserRepository userRepository;

    // Run daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateDailyMetrics() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // Fetch all user activities within the day in a single query
        List<UserActivity> allDailyActivities = userActivityRepository.findAllByDateRange(startOfDay, endOfDay);

        // Group activities by user and then by activity type
        Map<Long, Map<String, Long>> userActivitySummary = allDailyActivities.stream()
            .collect(Collectors.groupingBy(activity -> activity.getUser().getId(),
                    Collectors.groupingBy(UserActivity::getActivityType, Collectors.counting())));

        // Loop through each user's activity type counts and save metrics
        userActivitySummary.forEach((userId, activityTypeCounts) -> {
            activityTypeCounts.forEach((activityType, count) -> {
                performanceMetricService.saveDailyMetrics(userId, activityType, count.intValue());
            });
        });
    }
}
