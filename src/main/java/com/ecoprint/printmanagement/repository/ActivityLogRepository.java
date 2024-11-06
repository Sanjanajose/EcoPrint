package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByUserId(Long userId, Pageable pageable);

    List<ActivityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

	long countByUserIdAndAction(Long userId, String string);
}
