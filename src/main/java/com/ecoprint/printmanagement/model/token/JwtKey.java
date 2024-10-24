package com.ecoprint.printmanagement.model.token;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "jwt_keys")
public class JwtKey {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "secret_key", nullable = false)
	private String secretKey;
	
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public JwtKey(Long id, String secretKey, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.secretKey = secretKey;
		this.createdAt = createdAt;
	}

	public JwtKey() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
