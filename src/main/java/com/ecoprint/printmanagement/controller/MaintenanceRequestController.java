package com.ecoprint.printmanagement.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.dto.MaintenanceRequestDTO;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.MaintenanceRequest;
import com.ecoprint.printmanagement.model.MaintenanceStatus;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.UserDeviceRepository;
import com.ecoprint.printmanagement.service.MaintenanceRequestService;
import com.ecoprint.printmanagement.service.UserService;

@RestController
@RequestMapping("/api/maintenance-requests")
@PreAuthorize("hasRole('ADMIN')")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;
    
    @Autowired
    private UserService userservice;

    

    public MaintenanceRequestController(MaintenanceRequestService maintenanceRequestService) {
        this.maintenanceRequestService = maintenanceRequestService;
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaintenanceRequestDTO> submitMaintenanceRequest(
            @RequestParam Long printerId,
            @RequestParam String description) {

        Long userId = getCurrentUserId();
        User currentUser = userservice.getCurrentUser(userId);
        MaintenanceRequest request = maintenanceRequestService.createMaintenanceRequest(printerId, description, currentUser);
        MaintenanceRequestDTO dto = maintenanceRequestService.toDTO(request);

        return ResponseEntity.ok(dto);
    }

    private Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MaintenanceRequestDTO>> getMaintenanceRequests(
            @RequestParam MaintenanceStatus status,
            @RequestParam int page,
            @RequestParam int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MaintenanceRequestDTO> requests = maintenanceRequestService.getRequests(status, pageable);
        return ResponseEntity.ok(requests);
    }

    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_TECHNICIAN')")
    public ResponseEntity<MaintenanceRequestDTO> updateMaintenanceRequestStatus(
            @PathVariable Long id, @RequestParam String status) {
        MaintenanceRequestDTO dto = maintenanceRequestService.updateStatus(id, status);
        return ResponseEntity.ok(dto);
    }
}
