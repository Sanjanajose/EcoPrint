package com.ecoprint.printmanagement.task;

import com.ecoprint.printmanagement.model.ArchivedJob;
import com.ecoprint.printmanagement.model.CompletedJob;
import com.ecoprint.printmanagement.repository.ArchivedJobRepository;
import com.ecoprint.printmanagement.repository.CompletedJobRepository;
import com.ecoprint.printmanagement.service.SystemSettingsService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompletedJobCleanupTask {

    @Autowired
    private CompletedJobRepository completedJobRepository;

    @Autowired
    private ArchivedJobRepository archivedJobRepository;

    @Autowired
    private SystemSettingsService systemSettingsService;

    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Runs daily at 2:00 AM
    public void cleanOrArchiveOldCompletedJobs() {
        int retentionPeriod = systemSettingsService.getRetentionPeriodForJobs();
        String retentionAction = systemSettingsService.getRetentionAction();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionPeriod);

        List<CompletedJob> jobsToHandle = completedJobRepository.findByCompletedAtBefore(cutoffDate);

        if ("archive".equalsIgnoreCase(retentionAction)) {
            archiveJobs(jobsToHandle);
        } else if ("delete".equalsIgnoreCase(retentionAction)) {
            deleteJobs(jobsToHandle);
        }
    }

    private void archiveJobs(List<CompletedJob> jobsToArchive) {
        List<ArchivedJob> archivedJobs = jobsToArchive.stream()
            .map(this::convertToArchivedJob)
            .collect(Collectors.toList());
        archivedJobRepository.saveAll(archivedJobs);
        completedJobRepository.deleteAll(jobsToArchive);
    }

    private void deleteJobs(List<CompletedJob> jobsToDelete) {
        completedJobRepository.deleteAll(jobsToDelete);
    }

    private ArchivedJob convertToArchivedJob(CompletedJob completedJob) {
        ArchivedJob archivedJob = new ArchivedJob();
        archivedJob.setJobId(completedJob.getJobId());
        archivedJob.setUserId(completedJob.getUserId());
        archivedJob.setDescription(completedJob.getDescription());
        archivedJob.setPagesPrinted(completedJob.getPagesPrinted());
        archivedJob.setPrinterName(completedJob.getPrinterName());
        archivedJob.setSubmittedAt(completedJob.getSubmittedAt());
        archivedJob.setCompletedAt(completedJob.getCompletedAt());
        return archivedJob;
    }
}
