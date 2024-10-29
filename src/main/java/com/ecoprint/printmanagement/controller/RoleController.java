package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
