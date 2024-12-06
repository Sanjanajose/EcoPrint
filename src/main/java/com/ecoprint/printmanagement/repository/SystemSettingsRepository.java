package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.model.SystemSetting;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSetting, Long> {

   
    
    
    Optional<SystemSetting> findByKey(String key);
}
