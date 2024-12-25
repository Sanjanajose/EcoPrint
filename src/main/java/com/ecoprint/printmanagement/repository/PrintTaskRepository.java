package com.ecoprint.printmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.PrintTask;
@Repository

public interface PrintTaskRepository extends JpaRepository<PrintTask, Long> {
    List<PrintTask> findByStatus(String status);

}
