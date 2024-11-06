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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.config.RolePermissionMapping;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.Permission;
import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.repository.RoleRepository;

import jakarta.transaction.Transactional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
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
}
