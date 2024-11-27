package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.User;

public interface PrintJobRepository extends JpaRepository<PrintJob, Long> {

    // Method to find jobs by status, ordered by priority
    List<PrintJob> findByStatusOrderByPriorityAsc(PrintJobStatus status);

    // Method to find all jobs ordered by status and priority
    List<PrintJob> findAllByOrderByStatusAscPriorityAsc();
    
    @Query("SELECT MAX(p.queuePosition) FROM PrintJob p")
    Integer findMaxQueuePosition();
    
    @Query("SELECT p FROM PrintJob p WHERE p.queuePosition BETWEEN :start AND :end ORDER BY p.queuePosition")
    List<PrintJob> findByQueuePositionBetween(@Param("start") int start, @Param("end") int end);
    
 // Fetch jobs by a list of statuses
    List<PrintJob> findAllByStatusIn(List<PrintJobStatus> statuses);

   
    // Find PrintJob by file name
    Optional<PrintJob> findByFileName(String fileName);
    

    
    @Query("SELECT p FROM PrintJob p WHERE p.id = :jobId")
    Optional<PrintJob> findJobById(@Param("jobId") Long jobId);


    

    List<PrintJob> findByStatus(PrintJobStatus status);
    
    int countByStatusAndIdLessThan(PrintJobStatus status, Long id);




    
    // Query by user entity
    List<PrintJob> findByUser(User user);

    // Query by user ID
    @Query("SELECT p FROM PrintJob p WHERE p.user.id = :userId")
    List<PrintJob> findByUserId(@Param("userId") Long userId);

    
    @Query("SELECT p FROM PrintJob p WHERE p.user.id = :userId ORDER BY p.status ASC, p.priority ASC")
    List<PrintJob> findByUserIdOrderByStatusAscPriorityAsc(@Param("userId") Long userId);


    
    List<PrintJob> findByUserAndFavoriteTrue(User user);
    

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT j FROM PrintJob j WHERE j.favorite = true AND j.user.id = :userId")
    List<PrintJob> findFavoritesByUser(@Param("userId") Long userId);
    
   
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT j FROM PrintJob j WHERE j.favorite = true")
    List<PrintJob> findAllFavoritesWithUsers();
    
    @Query(value = "SELECT 1 FROM print_jobs j INNER JOIN users u ON j.user_id=u.user_id WHERE u.email = :userName AND j.id = :jobId",nativeQuery = true)
    Long existsByIdAndUser_Username(@Param("jobId") Long jobId, @Param("userName") String userName);
}
