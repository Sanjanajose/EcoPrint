package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class JobQueueService {

    @Autowired
    private PrintJobRepository printJobRepository;

    /**
     * Fetches all jobs and categorizes them by their status.
     *
     * @return A map categorizing jobs by their status.
     */
    public Map<String, List<PrintJob>> getQueueJobStatuses() {
        Map<String, List<PrintJob>> categorizedJobs = new HashMap<>();

        // Retrieve all jobs with the desired statuses
        List<PrintJob> allJobs = printJobRepository.findAllByStatusIn(
            Arrays.asList(
                PrintJobStatus.QUEUED,
                PrintJobStatus.PRINTING,
                PrintJobStatus.COMPLETED,
                PrintJobStatus.FAILED,
                PrintJobStatus.FAVORITE,
                PrintJobStatus.DELETED
            )
        );

        // Categorize jobs by their status
        for (PrintJob job : allJobs) {
            categorizedJobs
                .computeIfAbsent(job.getStatus().toString(), k -> new ArrayList<>())
                .add(job);
        }

        return categorizedJobs;
    }
}



