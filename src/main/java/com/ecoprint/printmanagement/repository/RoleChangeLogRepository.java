package com.ecoprint.printmanagement.repository;
 import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoprint.printmanagement.model.RoleChangeLog;

public interface RoleChangeLogRepository extends JpaRepository<RoleChangeLog, Long> {


}
