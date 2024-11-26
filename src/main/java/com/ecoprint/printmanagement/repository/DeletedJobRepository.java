package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.dto.DeletedJobResponse;
import com.ecoprint.printmanagement.model.DeletedJob;

import java.util.List;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedJobRepository extends JpaRepository<DeletedJob, Long> {

	// Fetch DeletedJob entities with optional filters
    @Query("SELECT d FROM DeletedJob d " +
           "WHERE (:startDate IS NULL OR d.deletedAt >= :startDate) " +
           "AND (:endDate IS NULL OR d.deletedAt <= :endDate) " +
           "AND (:userId IS NULL OR d.deletedBy.id = :userId)")
    List<DeletedJob> findDeletedJobs(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("userId") Long userId
    );

    // Fetch summaries as DTO
    @Query("SELECT new com.ecoprint.printmanagement.dto.DeletedJobResponse(" +
           "d.id, d.deletedAt, d.reasonForDeletion, d.printJob.fileName, d.deletedBy.username) " +
           "FROM DeletedJob d " +
           "WHERE (:startDate IS NULL OR d.deletedAt >= :startDate) " +
           "AND (:endDate IS NULL OR d.deletedAt <= :endDate) " +
           "AND (:userId IS NULL OR d.deletedBy.id = :userId)")
    List<DeletedJobResponse> findDeletedJobSummaries(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("userId") Long userId
    );
    
}
	




