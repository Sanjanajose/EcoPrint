package com.ecoprint.printmanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.PerformanceMetric;

@Repository
public interface PerformanceMetricRepository extends JpaRepository<PerformanceMetric, Long> {

    // Custom query to find metrics by user within a date range
    @Query("SELECT p FROM PerformanceMetric p WHERE p.user.id = :userId AND p.metricDate BETWEEN :start AND :end")
    List<PerformanceMetric> findMetricsByUserAndMonth(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end);
    
    
}
