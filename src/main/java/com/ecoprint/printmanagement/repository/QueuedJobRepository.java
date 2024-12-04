package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.model.QueuedJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueuedJobRepository extends JpaRepository<QueuedJob, Long> {
    // Add custom query methods if needed
}
