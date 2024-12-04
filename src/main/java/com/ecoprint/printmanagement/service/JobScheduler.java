package com.ecoprint.printmanagement.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {
	
	
    private final PrintJobManagementService printJobManagementService;

    public JobScheduler(PrintJobManagementService printJobManagementService) {
        this.printJobManagementService = printJobManagementService;
    }

    @Scheduled(fixedDelay = 60000) // Run every minute
    public void monitorPausedJobs() {
        printJobManagementService.autoResumeJobs();
    }


}
