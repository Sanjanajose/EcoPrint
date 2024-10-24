package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.token.JwtKey;

@Repository
public interface JwtKeyRepository extends JpaRepository<JwtKey, Long> {

	JwtKey findTopByOrderByCreatedAtDesc();

}
