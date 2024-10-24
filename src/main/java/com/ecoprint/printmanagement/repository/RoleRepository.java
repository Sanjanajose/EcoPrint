package com.ecoprint.printmanagement.repository;

<<<<<<< HEAD
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ecoprint.printmanagement.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Query that selects all roles except 'ROLE_SUPERADMIN'
    
	@Query("SELECT r FROM ROLE r WHERE r.role != com.ecoprint.printmanagement.model.RoleName.ROLE_SUPERADMIN")
    List<Role> findAllWithoutSuperAdmin();

}
=======
import com.ecoprint.printmanagement.model.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.ecoprint.printmanagement.model.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Correct query using 'role' instead of 'roleName'
    @Query("SELECT r FROM Role r WHERE r.role != 'ROLE_SUPERADMIN'")
    List<Role> findAllWithoutSuperAdmin();

    // Method to find a Role by its RoleName
        Optional<Role> findByRole(RoleName roleName);  // RoleName is the enum, not a String
        
       

    }


>>>>>>> 982c1c6 (Initial commit)
