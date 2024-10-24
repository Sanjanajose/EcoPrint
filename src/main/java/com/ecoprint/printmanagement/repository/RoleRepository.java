package com.ecoprint.printmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Query that selects all roles except 'ROLE_SUPERADMIN'
    @Query("SELECT r FROM Role r WHERE r.role != 'ROLE_SUPERADMIN'")
    List<Role> findAllWithoutSuperAdmin();

    // Method to find a Role by its RoleName
    Optional<Role> findByRole(RoleName roleName);  // RoleName is the enum, not a String
}
