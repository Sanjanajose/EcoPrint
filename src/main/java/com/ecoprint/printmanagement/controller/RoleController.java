package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.Permission;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeRoles() {
        roleService.initializeRoles();
        return ResponseEntity.ok("Roles and permissions initialized successfully.");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{roleName}/permissions/add")
    public ResponseEntity<String> addPermissionToRole(
            @PathVariable RoleName roleName,
            @RequestParam Permission permission) {
        roleService.addPermissionToRole(roleName, permission);
        return ResponseEntity.ok("Permission " + permission + " added to role " + roleName + " successfully.");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{roleName}/permissions/remove")
    public ResponseEntity<String> removePermissionFromRole(
            @PathVariable RoleName roleName,
            @RequestParam Permission permission) {
        roleService.removePermissionFromRole(roleName, permission);
        return ResponseEntity.ok("Permission " + permission + " removed from role " + roleName + " successfully.");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{roleName}/permissions")
    public ResponseEntity<Set<Permission>> getPermissionsForRole(@PathVariable RoleName roleName) {
        Set<Permission> permissions = roleService.getPermissionsForRole(roleName);
        return ResponseEntity.ok(permissions);
    }
}
