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
package com.ecoprint.printmanagement.controller;
import org.springframework.security.core.Authentication;


import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.annotation.CurrentUser;
import com.ecoprint.printmanagement.event.OnUserAccountChangeEvent;
import com.ecoprint.printmanagement.event.OnUserLogoutSuccessEvent;
import com.ecoprint.printmanagement.exception.FileUploadException;
import com.ecoprint.printmanagement.exception.UpdatePasswordException;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.payload.ApiResponse;
import com.ecoprint.printmanagement.model.payload.LogOutRequest;
import com.ecoprint.printmanagement.model.payload.RoleAssignmentRequest;
import com.ecoprint.printmanagement.model.payload.UpdatePasswordRequest;
import com.ecoprint.printmanagement.model.payload.UserUpdateRequest;
import com.ecoprint.printmanagement.service.AuthService;
import com.ecoprint.printmanagement.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Rest API", description = "Defines endpoints for the logged-in user. It's secured by default")
public class UserController {

    private final AuthService authService;
    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public UserController(AuthService authService, UserService userService, ApplicationEventPublisher applicationEventPublisher) {
        this.authService = authService;
        this.userService = userService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Returns the current user profile")
    public ResponseEntity<?> getUserProfile(@CurrentUser CustomUserDetails currentUser) {
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Returns the list of configured admins. Requires ADMIN Access")
    public ResponseEntity<?> getAllAdmins() {
        return ResponseEntity.ok("Hello. This is about admins");
    }

    @PostMapping("/password/update")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Allows the user to change their password once logged in by supplying the correct current password")
    public ResponseEntity<?> updateUserPassword(@CurrentUser CustomUserDetails customUserDetails,
                                             @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        return authService.updatePassword(customUserDetails, updatePasswordRequest)
                .map(updatedUser -> {
                    OnUserAccountChangeEvent onUserPasswordChangeEvent = new OnUserAccountChangeEvent(updatedUser,
                            "Update Password", "Change successful");
                    applicationEventPublisher.publishEvent(onUserPasswordChangeEvent);
                    return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
                })
                .orElseThrow(() -> new UpdatePasswordException("--Empty--", "No such user present."));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logs the specified user device and clears the refresh tokens associated with it")
    public ResponseEntity<?> logoutUser(@CurrentUser CustomUserDetails customUserDetails,
                                     @Valid @RequestBody LogOutRequest logOutRequest) {
        userService.logoutUser(customUserDetails, logOutRequest);
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();

        OnUserLogoutSuccessEvent logoutSuccessEvent = new OnUserLogoutSuccessEvent(customUserDetails.getEmail(),
                credentials.toString(), logOutRequest);
        applicationEventPublisher.publishEvent(logoutSuccessEvent);
        return ResponseEntity.ok(new ApiResponse(true, "Log out successful"));
    }

    @GetMapping("/resetPassword")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset Password with a temporary password for the given user")
    public ResponseEntity<?> resetPassword(@RequestParam String email) {
        authService.resetWithTempPassword(email);
        return ResponseEntity.ok(new ApiResponse(true, "Temporary password set successfully"));
    }

    @GetMapping("/setAdminAccess")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @Operation(summary = "Grants admin access to a user. Requires SUPERADMIN access.")
    public ResponseEntity<?> setAdminAccess(@RequestParam String email) {
        userService.setAdminAccess(email);
        return ResponseEntity.ok(new ApiResponse(true, "Admin Access Given Successfully"));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Allows the logged-in user to update their own profile")
    public ResponseEntity<?> updateOwnProfile(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestPart("user") UserUpdateRequest updateRequest,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        Long userId = currentUser.getId();

        if (userService.existsByEmail(updateRequest.getEmail()) && !currentUser.getEmail().equals(updateRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email is already in use."));
        }

        if (profilePicture != null && !profilePicture.isEmpty()) {
            long maxFileSize = 5 * 1024 * 1024;
            if (profilePicture.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "File size exceeds the maximum limit of 5MB."));
            }

            Set<String> allowedContentTypes = Set.of("image/jpeg", "image/png");
            if (!allowedContentTypes.contains(profilePicture.getContentType())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid file type. Only JPEG and PNG are allowed."));
            }

            try {
                String profilePictureUrl = userService.uploadProfilePicture(profilePicture, 300, 300);
                updateRequest.setProfilePicture(profilePictureUrl);
            } catch (FileUploadException e) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
            }
        }

        Set<String> currentRoles = currentUser.getAuthorities().stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.toSet());

        User updatedUserData = new User();
        updatedUserData.setUsername(updateRequest.getUsername());
        updatedUserData.setEmail(updateRequest.getEmail());
        updatedUserData.setPhone(updateRequest.getPhone());
        updatedUserData.setAddress(updateRequest.getAddress());
        updatedUserData.setGender(updateRequest.getGender());
        updatedUserData.setCountry(updateRequest.getCountry());
        updatedUserData.setDob(updateRequest.getDob());

        if (updateRequest.getProfilePicture() != null) {
            updatedUserData.setProfilePicture(updateRequest.getProfilePicture());
        }

        User updatedUser = userService.updateUser(userId, updatedUserData, currentRoles);

        OnUserAccountChangeEvent userUpdateEvent = new OnUserAccountChangeEvent(updatedUser, 
            "Update Profile", "User updated their own profile successfully");
        applicationEventPublisher.publishEvent(userUpdateEvent);

        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully."));
    }
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/{userId}/assignRoles")
    public ResponseEntity<ApiResponse> assignRolesToUser(
            @PathVariable Long userId,
            @RequestBody RoleAssignmentRequest roleAssignmentRequest,
            Authentication authentication) {

        userService.assignRolesToUser(userId, roleAssignmentRequest.getRoleNames());

        return ResponseEntity.ok(new ApiResponse(true, "Roles assigned successfully"));
    }

    
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping("/{userId}/deleteRole")
    public ResponseEntity<ApiResponse> deleteUserRole(
            @PathVariable Long userId,
            @RequestParam String roleName,
            Authentication authentication) {

        userService.deleteUserRole(userId, roleName, authentication);

        return ResponseEntity.ok(new ApiResponse(true, "Role removed successfully"));
    }

    

    
    
}
