package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.Permission;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.service.RoleService;
import java.util.List; 


import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @Operation(summary = "allows SUPER ADMIN to initialize roles and permissions")
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeRoles() {
        roleService.initializeRoles();
        return ResponseEntity.ok("Roles and permissions initialized successfully.");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{roleName}/permissions/add")
    @Operation(summary = "allows ADMIN to add permissions to roles")
    public ResponseEntity<String> addPermissionToRole(
            @PathVariable RoleName roleName,
            @RequestParam Permission permission) {
        roleService.addPermissionToRole(roleName, permission);
        return ResponseEntity.ok("Permission " + permission + " added to role " + roleName + " successfully.");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "allows ADMIN to add permissions to roles")
    @PostMapping("/{roleName}/permissions/remove")
    public ResponseEntity<String> removePermissionFromRole(
            @PathVariable RoleName roleName,
            @RequestParam Permission permission) {
        roleService.removePermissionFromRole(roleName, permission);
        return ResponseEntity.ok("Permission " + permission + " removed from role " + roleName + " successfully.");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{roleName}/permissions")
    @Operation(summary = "allows ADMIN to get the list of permissions assigned to a role")
    public ResponseEntity<Set<Permission>> getPermissionsForRole(@PathVariable RoleName roleName) {
        Set<Permission> permissions = roleService.getPermissionsForRole(roleName);
        return ResponseEntity.ok(permissions);
    }
    
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "allows ADMIN  and SUPER ADMIN to assign roles to a user in a company ")
    @PostMapping("/assignRole")
     public ResponseEntity<?> assignRole(
             @RequestParam Long companyId,
             @RequestParam Long userId,
             @RequestParam Long roleId,
             @RequestHeader("X-Requested-By") Long requestedBy) {

         roleService.assignRole(userId, roleId, requestedBy, companyId);
         return ResponseEntity.ok("Role assigned successfully");
     }

    
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/revokeRole")
    @Operation(summary = "allows ADMIN  and SUPER ADMIN to revoke roles of a user in a company ")
    public ResponseEntity<?> revokeRole(
            @RequestParam Long companyId,
            @RequestParam Long userId,
            @RequestParam Long roleId,
            @RequestHeader("X-Requested-By") Long requestedBy) {

        roleService.revokeRole(userId, roleId, requestedBy, companyId);
        return ResponseEntity.ok("Role revoked successfully");
    }

    @Operation(summary = "Fetch all available roles")
    @GetMapping("/roles/all")
    public ResponseEntity<List<String>> getAllRoles() {
        List<String> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    
}
