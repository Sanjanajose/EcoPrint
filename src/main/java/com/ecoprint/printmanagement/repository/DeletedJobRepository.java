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

    
    
	@Query("""
		    SELECT new com.ecoprint.printmanagement.dto.DeletedJobResponse(
		        d.id, 
		        d.deletedAt, 
		        db.username, 
		        d.reasonForDeletion,
		        d.previousStatus,
		        d.restorableUntil
		    )
		    FROM DeletedJob d 
		    JOIN d.deletedBy db 
		    WHERE (:startDate IS NULL OR d.deletedAt >= :startDate)
		    AND (:endDate IS NULL OR d.deletedAt <= :endDate)
		    AND (:userId IS NULL OR db.id = :userId)
		""")
		List<DeletedJobResponse> findDeletedJobSummaries(
		    @Param("startDate") LocalDateTime startDate, 
		    @Param("endDate") LocalDateTime endDate, 
		    @Param("userId") Long userId
		);

    
    // For Admin: Fetch all deleted jobs
    @Query("SELECT dj FROM DeletedJob dj WHERE " +
            "(dj.deletedAt BETWEEN :startDate AND :endDate OR :startDate IS NULL OR :endDate IS NULL) " +
            "AND (dj.deletedBy.id = :deletedByUserId OR :deletedByUserId IS NULL) " +
            "ORDER BY dj.deletedAt DESC")
    List<DeletedJob> findDeletedJobs(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("deletedByUserId") Long deletedByUserId
    );

    // For User: Fetch their deleted jobs
    @Query("SELECT dj FROM DeletedJob dj WHERE " +
            "dj.deletedBy.id = :userId AND " +
            "(dj.deletedAt BETWEEN :startDate AND :endDate OR :startDate IS NULL OR :endDate IS NULL) " +
            "ORDER BY dj.deletedAt DESC")
    List<DeletedJob> findDeletedJobsByUser(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    
    @Query("SELECT new com.ecoprint.printmanagement.dto.DeletedJobResponse(d.id, d.deletedAt, u.username, d.reasonForDeletion, d.previousStatus, d.restorableUntil) " +
    	       "FROM DeletedJob d JOIN d.deletedBy u " +
    	       "WHERE (:startDate IS NULL OR d.deletedAt >= :startDate) " +
    	       "AND (:endDate IS NULL OR d.deletedAt <= :endDate) " +
    	       "AND u.id = :userId")
    	List<DeletedJobResponse> findDeletedJobsForUser(@Param("startDate") LocalDateTime startDate,
    	                                                @Param("endDate") LocalDateTime endDate,
    	                                                @Param("userId") Long userId);

    
}
	




