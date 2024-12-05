package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.model.QueuedJob;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueuedJobRepository extends JpaRepository<QueuedJob, Long> {

    @Query("SELECT MAX(q.queuePosition) FROM QueuedJob q")
    Integer findMaxQueuePosition();
    
    
    @Query("SELECT q FROM QueuedJob q WHERE q.queuePosition IS NOT NULL ORDER BY q.queuePosition ASC")
    List<QueuedJob> findAllByOrderByQueuePosition();
    
    
    @Query("SELECT q FROM QueuedJob q WHERE q.queuePosition BETWEEN :start AND :end ORDER BY q.queuePosition ASC")
    List<QueuedJob> findByQueuePositionBetween(@Param("start") int start, @Param("end") int end);

    
}
