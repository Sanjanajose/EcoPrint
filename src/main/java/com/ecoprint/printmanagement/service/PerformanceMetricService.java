package com.ecoprint.printmanagement.service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecoprint.printmanagement.model.PerformanceMetric;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.PerformanceMetricRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class PerformanceMetricService {

    @Autowired
    private PerformanceMetricRepository performanceMetricRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveDailyMetrics(Long userId, String metricType, Integer metricValue) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        PerformanceMetric metric = new PerformanceMetric();
        metric.setUser(user);
        metric.setMetricType(metricType);
        metric.setMetricValue(metricValue);
        metric.setMetricDate(LocalDate.now());
        performanceMetricRepository.save(metric);
    }

    
    
    @Transactional(readOnly = true)
    public List<PerformanceMetric> getPerformanceMetrics(Long userId, LocalDate start, LocalDate end) {
        List<PerformanceMetric> metrics = performanceMetricRepository.findMetricsByUserAndMonth(userId, start, end);
        metrics.forEach(metric -> Hibernate.initialize(metric.getUser().getBackupCodes())); // Initialize specific lazy-loaded collection if needed
        return metrics;
    }

}
