package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.model.ArchivedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedJobRepository extends JpaRepository<ArchivedJob, Long> {
    // Add custom queries if needed
}
