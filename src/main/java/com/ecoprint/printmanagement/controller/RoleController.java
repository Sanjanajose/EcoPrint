package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.Permission;
import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.service.RoleService;

import java.util.List; 


import com.ecoprint.printmanagement.service.UserService;


import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.*;

import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final RoleService roleService;
    private final UserService userService;


    @Autowired
    public RoleController(RoleService roleService,UserService userService) {
        this.roleService = roleService;
        		this.userService=userService;
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
    
   /* 
    @GetMapping("/current/roles")
    @Operation(summary = "Fetch the roles of the currently authenticated user")
    public ResponseEntity<Set<RoleName>> getRolesForCurrentUser(Authentication authentication) {
        String username = authentication.getName(); // Fetch username from authentication context
        System.out.println("username:::"+username);
        Long userId = userService.getUserIdByUsername(username); // Fetch userId using the username
        Set<RoleName> roles = roleService.getRolesByUserId(userId);
        return ResponseEntity.ok(roles);
    }*/
    
    
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/current/roles")
    @Operation(summary = "Fetch the roles of the currently authenticated user")
    public ResponseEntity<Set<RoleName>> getRolesForCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // Fetch email from authentication context
        User currentUser = userService.getUserByEmail(email); // Use service to fetch user by email
        Set<RoleName> roles = currentUser.getRoles().stream()
                .map(Role::getRole)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(roles);
    }


    @Operation(summary = "Fetch all available roles")
    @GetMapping("/roles/all")
    public ResponseEntity<List<String>> getAllRoles() {
        List<String> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    
}
