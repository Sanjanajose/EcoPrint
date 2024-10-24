package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    // Custom query methods (if needed) can be defined here
}