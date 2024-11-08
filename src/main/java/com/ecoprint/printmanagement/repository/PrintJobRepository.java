package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;

public interface PrintJobRepository extends JpaRepository<PrintJob, Long> {

    // Method to find jobs by status, ordered by priority
    List<PrintJob> findByStatusOrderByPriorityAsc(PrintJobStatus status);

    // Method to find all jobs ordered by status and priority
    List<PrintJob> findAllByOrderByStatusAscPriorityAsc();
}
