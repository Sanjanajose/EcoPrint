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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.config.RolePermissionMapping;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.CompanyDetails;
import com.ecoprint.printmanagement.model.Permission;
import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleChangeLog;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.CompanyAccountRepository;
import com.ecoprint.printmanagement.repository.RoleChangeLogRepository;
import com.ecoprint.printmanagement.repository.RoleRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

import io.jsonwebtoken.lang.Arrays;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CompanyAccountRepository companyRepository;
    private final RoleChangeLogRepository roleChangeLogRepository;



    @Autowired
    public RoleService(UserRepository userRepository,RoleRepository roleRepository,CompanyAccountRepository companyRepository,RoleChangeLogRepository roleChangeLogRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyRepository=companyRepository;
        this.roleChangeLogRepository=roleChangeLogRepository;
    }

    /**
     * Find all roles from the database.
     */
    public Collection<Role> findAll() {
        return roleRepository.findAll();
    }

    /**
     * Find all roles without super admin.
     */
    public List<Role> findAllWithoutSuperAdmin() {
        return roleRepository.findAllWithoutSuperAdmin();
    }

    /**
     * Find a role by its name (as an enum).
     */
    public Optional<Role> findByRole(RoleName roleName) {
        return roleRepository.findByRole(roleName);
    }

    /**
     * Get a role by name, throwing exception if not found.
     */
    public Role getRoleByName(RoleName roleName) {
        return findByRole(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
    }

    /**
     * Method to get roles by a set of names.
     */
    public Set<Role> getRolesByName(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(this::getRoleByName)
                .collect(Collectors.toSet());
    }

    /**
     * Find a role by name as a string.
     */
    public Optional<Role> findByName(String roleName) {
        RoleName roleEnum;
        try {
            roleEnum = RoleName.valueOf(roleName);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        return roleRepository.findByRole(roleEnum);
    }
    
    @Transactional
    public void initializeRoles() {
        RolePermissionMapping.rolePermissions.forEach((roleName, permissions) -> {
            roleRepository.findByRole(roleName).ifPresentOrElse(
                role -> {
                    Set<Permission> currentPermissions = role.getPermissions();
                    if (!currentPermissions.containsAll(permissions)) {
                        currentPermissions.addAll(permissions);
                        role.setPermissions(currentPermissions);
                        roleRepository.save(role);
                    }
                },
                () -> {
                    Role newRole = new Role(roleName);
                    newRole.setPermissions(permissions);
                    roleRepository.save(newRole);
                }
            );
        });
    }

    /**
     * Add a permission to a role.
     */
    @Transactional
    public void addPermissionToRole(RoleName roleName, Permission permission) {
        Role role = getRoleByName(roleName);
        Set<Permission> permissions = role.getPermissions();
        if (!permissions.contains(permission)) {
            permissions.add(permission);
            role.setPermissions(permissions);
            roleRepository.save(role);
        }
    }

    /**
     * Remove a permission from a role.
     */
    @Transactional
    public void removePermissionFromRole(RoleName roleName, Permission permission) {
        Role role = getRoleByName(roleName);
        Set<Permission> permissions = role.getPermissions();
        if (permissions.contains(permission)) {
            permissions.remove(permission);
            role.setPermissions(permissions);
            roleRepository.save(role);
        }
    }

    /**
     * Get all permissions for a role.
     */
    public Set<Permission> getPermissionsForRole(RoleName roleName) {
        Role role = getRoleByName(roleName);
        return role.getPermissions();
    }
    
    @Transactional
    public void assignRole(Long userId, Long roleId, Long requestedById, Long companyId) {
        User requestingUser = userRepository.findById(requestedById)
                .orElseThrow(() -> new EntityNotFoundException("Requesting user not found with ID: " + requestedById));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + roleId));
        CompanyDetails company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with ID: " + companyId));
        
        Boolean isCompanyAdmin = userRepository.hasRole(requestedById, RoleName.ROLE_ADMIN)&&
                requestingUser.getCompanyDetails() != null &&
                requestingUser.getCompanyDetails().getCompanyId().equals(companyId);
        
        Boolean isSuperAdmin = userRepository.hasRole(requestedById, RoleName.ROLE_SUPERADMIN);



        if (isSuperAdmin || isCompanyAdmin) {
     
            user.setCompanyDetails(company);
            

            // Assign the new role
            user.addRole(role);
            userRepository.save(user);
           logRoleChange(user, role, "ASSIGN", requestedById); // Implement logging for compliance
       } else {
            throw new AccessDeniedException("Only Super Admins or Company Admins can assign roles.");
        }
    }
    
    
    
    @Transactional
    public void revokeRole(Long userId, Long roleId, Long requestedById, Long companyId) {
        // Fetch the user, role, and company
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + roleId));
        CompanyDetails company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with ID: " + companyId));

        // Check if the requesting user has permission to revoke the role
        boolean isSuperAdmin = userRepository.hasRole(requestedById, RoleName.ROLE_SUPERADMIN);
        boolean isCompanyAdmin = userRepository.hasRole(requestedById, RoleName.ROLE_ADMIN) &&
                                 userRepository.findById(requestedById).get().getCompanyDetails().getCompanyId().equals(companyId);

        if (isSuperAdmin || isCompanyAdmin) {
            // If the user has the role, remove it
            if (user.getRoles().contains(role)) {
                user.getRoles().remove(role);
                userRepository.save(user);

                // Log the role revocation action for compliance
               logRoleChange(user, role, "REVOKE", requestedById);
            } else {
                throw new IllegalStateException("User does not have the specified role");
            }
        } else {
            throw new AccessDeniedException("Only Super Admins or Company Admins can revoke roles.");
        }
    }

    
    private void logRoleChange(User user, Role role, String action, Long requestedById) {
        // Log the role assignment or revocation action (e.g., save to a database table or external log service)
        RoleChangeLog log = new RoleChangeLog();
        log.setUserId(user.getId());
        log.setRoleId(role.getId());
        log.setAction(action);
        log.setRequestedBy(requestedById);
        log.setTimestamp(new Date());
        roleChangeLogRepository.save(log);
    }
    
    public List<String> getAllRoles() {
        List<String> roles = new ArrayList<>();
        for (RoleName roleName : RoleName.values()) {
            roles.add(roleName.name());
        }
        return roles;
    }

    
}
