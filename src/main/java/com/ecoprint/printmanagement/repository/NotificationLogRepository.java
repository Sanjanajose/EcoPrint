package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.NotificationLog;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    // Custom queries, if needed
}
