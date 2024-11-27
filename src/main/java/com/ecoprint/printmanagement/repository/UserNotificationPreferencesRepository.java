package com.ecoprint.printmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.UserNotificationPreferences;

@Repository
public interface UserNotificationPreferencesRepository extends JpaRepository<UserNotificationPreferences, Long> {
	

	 Optional<UserNotificationPreferences> findByUserId(Long userId);
	 
	 
	 @Query("SELECT p FROM UserNotificationPreferences p WHERE p.user.email = :email")
	    Optional<UserNotificationPreferences> findByUserEmail(@Param("email") String email);


	 @Modifying
	 @Query("DELETE FROM UserNotificationPreferences u WHERE u.user.id = :userId")
	 void deleteByUserId(@Param("userId") Long userId);


	}
	


