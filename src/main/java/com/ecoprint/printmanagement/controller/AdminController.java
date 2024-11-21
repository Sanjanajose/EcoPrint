package com.ecoprint.printmanagement.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.event.OnUserAccountChangeEvent;
import com.ecoprint.printmanagement.exception.FileUploadException;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.ActivityLog;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.payload.ApiResponse;
import com.ecoprint.printmanagement.model.payload.RoleAssignmentRequest;
import com.ecoprint.printmanagement.model.payload.UserUpdateRequest;
import com.ecoprint.printmanagement.model.payload.RegistrationRequest;
import com.ecoprint.printmanagement.service.AdminService;
import com.ecoprint.printmanagement.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import jakarta.mail.MessagingException;
import io.jsonwebtoken.io.IOException;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService; // Ensure this line is present
    private final ApplicationEventPublisher applicationEventPublisher; // Add this line

    @Autowired
    public AdminController(AdminService adminService, UserService userService, ApplicationEventPublisher applicationEventPublisher) {
        this.adminService = adminService;
        this.userService = userService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<User> users = adminService.getAllUsers(pageNumber, pageSize);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    @Operation(summary = "Create a new user with assigned roles")
    public ResponseEntity<?> createUser(
            @Valid RegistrationRequest registrationRequest,
            @RequestParam Set<String> roles) { // Accept roles as a request parameter

        // Call admin service to create the user with the provided roles
        User newUser = adminService.createUser(registrationRequest, roles);
        return ResponseEntity.ok(new ApiResponse(true, "User created successfully."));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @Operation(summary = "Allows an admin or superadmin to update the user's details and roles")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestPart("user") UserUpdateRequest updateRequest,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        // Validate that the email isn't already in use by another user
        if (userService.existsByEmail(updateRequest.getEmail()) && !userService.isSameUser(id, updateRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email is already in use."));
        }

        // Validate and upload profile picture if provided
        if (profilePicture != null && !profilePicture.isEmpty()) {
            // Validate file size (5MB limit)
            long maxFileSize = 5 * 1024 * 1024;
            if (profilePicture.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "File size exceeds the maximum limit of 5MB."));
            }

            // Validate file type (JPEG, PNG)
            Set<String> allowedContentTypes = Set.of("image/jpeg", "image/png");
            if (!allowedContentTypes.contains(profilePicture.getContentType())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid file type. Only JPEG and PNG are allowed."));
            }

            // Handle the upload logic
            try {
                String profilePictureUrl = userService.uploadProfilePicture(profilePicture, 300, 300);
                updateRequest.setProfilePicture(profilePictureUrl);  // Set the profile picture URL
            } catch (IOException e) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "File upload failed: " + e.getMessage()));
            }
        }

        // Update other user details
        User updatedUserData = new User();
        updatedUserData.setUsername(updateRequest.getUsername());
        updatedUserData.setEmail(updateRequest.getEmail());
        updatedUserData.setPhone(updateRequest.getPhone());
        updatedUserData.setAddress(updateRequest.getAddress());
        updatedUserData.setGender(updateRequest.getGender());
        updatedUserData.setCountry(updateRequest.getCountry());
        updatedUserData.setDob(updateRequest.getDob());
        updatedUserData.setProfilePicture(updateRequest.getProfilePicture());

        // Convert the role strings to RoleName enum and validate
        Set<RoleName> roleNames = updateRequest.getRoles().stream()
                .map(roleName -> {
                    try {
                        return RoleName.valueOf(roleName);
                    } catch (IllegalArgumentException e) {
                        throw new ResourceNotFoundException("Role", "name", roleName); // Handle invalid role name
                    }
                })
                .collect(Collectors.toSet());

        // Convert the RoleName enum back to String for the updateUser method
        Set<String> newRoles = roleNames.stream()
                .map(RoleName::name) // Get the name of the RoleName enum
                .collect(Collectors.toSet());

        // Update user in the service
        User updatedUser = userService.updateUserByAdmin(id, updatedUserData, newRoles);

        // Trigger user account update event
        OnUserAccountChangeEvent userUpdateEvent = new OnUserAccountChangeEvent(updatedUser, "Update User", "Details updated successfully");
        applicationEventPublisher.publishEvent(userUpdateEvent);

        return ResponseEntity.ok(new ApiResponse(true, "User updated successfully."));
    }

    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @Operation(summary = "Assign roles to a user")
    public ResponseEntity<?> assignRoles(@PathVariable Long id, @RequestBody RoleAssignmentRequest roleRequest) {
        // Assign roles using the admin service
        User updatedUser = adminService.assignRoles(id, roleRequest);
        return ResponseEntity.ok(new ApiResponse(true, "Roles assigned successfully."));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully."));
    }

    @GetMapping("/activity-logs")
    @Operation(summary = "Retrieve system activity logs")
    public ResponseEntity<?> getActivityLogs(@RequestParam(defaultValue = "0") int page, 
                                            @RequestParam(defaultValue = "10") int size) {
        List<ActivityLog> logs = adminService.getActivityLogs(page, size);
        return ResponseEntity.ok(logs);
    }
}
