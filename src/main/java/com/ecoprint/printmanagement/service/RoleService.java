/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ecoprint.printmanagement.service;

import java.util.Collection;
import java.util.List;
<<<<<<< HEAD
=======
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
>>>>>>> 982c1c6 (Initial commit)

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
import com.ecoprint.printmanagement.model.Role;
=======
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
>>>>>>> 982c1c6 (Initial commit)
import com.ecoprint.printmanagement.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

<<<<<<< HEAD
    /**
     * Find all roles from the database
     */
=======
    // Find all roles from the database
>>>>>>> 982c1c6 (Initial commit)
    public Collection<Role> findAll() {
        return roleRepository.findAll();
    }

<<<<<<< HEAD
	public List<Role> findAllWithoutSuperAdmin() {
		return roleRepository.findAllWithoutSuperAdmin();
	}
=======
    // Find all roles without super admin
    public List<Role> findAllWithoutSuperAdmin() {
        return roleRepository.findAllWithoutSuperAdmin();
    }

    // Find a role by its name (as an enum)
    public Optional<Role> findByRole(RoleName roleName) {
        return roleRepository.findByRole(roleName);
    }

    // Get a role by name, throwing exception if not found
    public Role getRoleByName(RoleName roleName) {
        return findByRole(roleName) // Use findByRole for safer handling
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
    }

    // Method to get roles by a set of names
    public Set<Role> getRolesByName(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(this::getRoleByName) // Use getRoleByName for retrieval
                .collect(Collectors.toSet());
    }
    
    public Optional<Role> findByName(String roleName) {
        RoleName roleEnum;
        try {
            roleEnum = RoleName.valueOf(roleName);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        return roleRepository.findByRole(roleEnum); // Make sure this matches
    }



    
>>>>>>> 982c1c6 (Initial commit)

}
