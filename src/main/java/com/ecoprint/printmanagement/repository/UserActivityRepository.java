package com.ecoprint.printmanagement.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.UserActivity;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

	@Query("SELECT ua FROM UserActivity ua JOIN FETCH ua.user u WHERE ua.user.id = :userId AND ua.timestamp BETWEEN :start AND :end")
	   List<UserActivity> findActivitiesByUserAndDateRange(
	       @Param("userId") Long userId,
	       @Param("start") LocalDateTime start,
	       @Param("end") LocalDateTime end);
	
	// New method to retrieve activities for all users within a date range
    @Query("SELECT ua FROM UserActivity ua JOIN FETCH ua.user WHERE ua.timestamp BETWEEN :start AND :end")
    List<UserActivity> findAllByDateRange(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}
