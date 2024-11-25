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
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import com.ecoprint.printmanagement.model.payload.RegistrationRequest;
import com.ecoprint.printmanagement.model.payload.RoleAssignmentRequest;
import com.ecoprint.printmanagement.model.payload.UpdatePasswordRequest;
import com.ecoprint.printmanagement.model.payload.UserUpdateRequest;
import com.ecoprint.printmanagement.service.AuthService;
import com.ecoprint.printmanagement.service.UserActivityService;
import com.ecoprint.printmanagement.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private UserActivityService userActivityService;

    @Autowired
    public UserController(AuthService authService, UserService userService, ApplicationEventPublisher applicationEventPublisher) {
        this.authService = authService;
        this.userService = userService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Gets the current user profile of the logged in user
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Returns the current user profile")
    public ResponseEntity getUserProfile(@CurrentUser CustomUserDetails currentUser) {
//        logger.info(currentUser.getEmail() + " has role: " + currentUser.getRoles());
        return ResponseEntity.ok("Hello. This is about me");
    }    

    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Returns the list of configured admins. Requires ADMIN Access")
    public ResponseEntity<?> getAllAdmins() {
        return ResponseEntity.ok("Hello. This is about admins");
    }

    /**
     * Updates the password of the current logged in user
     */
    /**@PostMapping("/password/update")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Allows the user to change his password once logged in by supplying the correct current " +
            "password")
    public ResponseEntity updateUserPassword(@CurrentUser CustomUserDetails customUserDetails,
                                             @Param(value = "The UpdatePasswordRequest payload") @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {

        return authService.updatePassword(customUserDetails, updatePasswordRequest)
                .map(updatedUser -> {
                    OnUserAccountChangeEvent onUserPasswordChangeEvent = new OnUserAccountChangeEvent(updatedUser,
                            "Update Password", "Change successful");
                    applicationEventPublisher.publishEvent(onUserPasswordChangeEvent);
                    return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
                })
                .orElseThrow(() -> new UpdatePasswordException("--Empty--", "No such user present."));
    }**/
    
    @PostMapping("/password/update")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Allows the user to change their password once logged in by supplying the correct current password")
    public ResponseEntity<ApiResponse> updateUserPassword(
            @CurrentUser CustomUserDetails customUserDetails,
            @Param(value = "The UpdatePasswordRequest payload") @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {

        return authService.updatePassword(customUserDetails, updatePasswordRequest)
                .map(updatedUser -> {
                    // Log password update activity
                    userActivityService.logUserActivity(updatedUser.getId(), "PASSWORD_UPDATE", "User password changed successfully");

                    // Trigger an event for password update success
                    OnUserAccountChangeEvent onUserPasswordChangeEvent = new OnUserAccountChangeEvent(
                            updatedUser, "Update Password", "Change successful");
                    applicationEventPublisher.publishEvent(onUserPasswordChangeEvent);
                    
                    // Return success response
                    return new ApiResponse(true, "Password changed successfully");
                })
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new UpdatePasswordException("--Empty--", "No such user present."));
    }



    /**@PostMapping("/logout")
    @Operation(summary = "Logs the specified user device and clears the refresh tokens associated with it")
    public ResponseEntity<?> logoutUser(@CurrentUser CustomUserDetails customUserDetails,
                                     @Valid @RequestBody LogOutRequest logOutRequest) {
        userService.logoutUser(customUserDetails, logOutRequest);
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();

        OnUserLogoutSuccessEvent logoutSuccessEvent = new OnUserLogoutSuccessEvent(customUserDetails.getEmail(),
                credentials.toString(), logOutRequest);
        applicationEventPublisher.publishEvent(logoutSuccessEvent);
        return ResponseEntity.ok(new ApiResponse(true, "Log out successful"));
    } **/
    
    @PostMapping("/logout")
    @Operation(summary = "Logs the specified user device and clears the refresh tokens associated with it")
    public ResponseEntity<?> logoutUser(@CurrentUser CustomUserDetails customUserDetails,
                                        @Valid @RequestBody LogOutRequest logOutRequest) {
        // Call the service to perform logout actions
        userService.logoutUser(customUserDetails, logOutRequest);

        // Log the logout activity for tracking
        userActivityService.logUserActivity(customUserDetails.getId(), "LOGOUT", "User logged out successfully");

        // Publish a logout success event
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        OnUserLogoutSuccessEvent logoutSuccessEvent = new OnUserLogoutSuccessEvent(customUserDetails.getEmail(),
                credentials.toString(), logOutRequest);
        applicationEventPublisher.publishEvent(logoutSuccessEvent);

        // Return a successful logout response
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

    /**
     * Updates the logged-in user's own profile.
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Allows the logged-in user to update their own profile")
    public ResponseEntity<?> updateOwnProfile(
    		@CurrentUser CustomUserDetails currentUser, // CustomUserDetails to get the current logged-in user
    	    		@Valid @RequestParam("user") String updateRequest,
    	    		@RequestParam("profilePic") @io.swagger.v3.oas.annotations.media.Schema(description = "Profile picture to upload", type = "string", format = "binary") MultipartFile profilePic
    	    		) throws JsonMappingException, JsonProcessingException {
    	    	ObjectMapper objectMapper = new ObjectMapper();
    	    	objectMapper.registerModule(new JavaTimeModule());
    	    	UserUpdateRequest request = objectMapper.readValue(updateRequest, UserUpdateRequest.class);

        Long userId = currentUser.getId();

        // Validate that the email isn't already in use by another user
        if (userService.existsByEmail(request.getEmail()) && !currentUser.getEmail().equals(request.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email is already in use."));
        }

        // Validate and upload profile picture if provided
        if (profilePic != null && !profilePic.isEmpty()) {
            // Validate file size (5MB limit)
            long maxFileSize = 5 * 1024 * 1024;
            if (profilePic.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "File size exceeds the maximum limit of 5MB."));
            }

            // Validate file type (JPEG, PNG)
            Set<String> allowedContentTypes = Set.of("image/jpeg", "image/png");
            if (!allowedContentTypes.contains(profilePic.getContentType())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid file type. Only JPEG and PNG are allowed."));
            }

            // Handle the upload logic
            try {
                String profilePictureUrl = userService.uploadProfilePicture(profilePic, 300, 300);
                request.setProfilePicture(profilePictureUrl);  // Set the profile picture URL
            } catch (FileUploadException e) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
            }
        }

        // Prevent the user from updating their own roles
//        Set<String> currentRoles = currentUser.getAuthorities().stream()
//                .map(role -> role.getAuthority())
//                .collect(Collectors.toSet());

        // Update user details
        User updatedUserData = new User();
        updatedUserData.setUsername(request.getUsername());
        updatedUserData.setEmail(request.getEmail());
        updatedUserData.setPhone(request.getPhone());
        updatedUserData.setAddress(request.getAddress());
        updatedUserData.setGender(request.getGender());
        updatedUserData.setCountry(request.getCountry());
        updatedUserData.setDob(request.getDob());

        // Set updated profile picture if available
        if (request.getProfilePicture() != null) {
            updatedUserData.setProfilePicture(request.getProfilePicture());
        }

        // Update user in the service layer
        User updatedUser = userService.updateUser(userId, updatedUserData);

        // Trigger user account change event
        OnUserAccountChangeEvent userUpdateEvent = new OnUserAccountChangeEvent(updatedUser, 
            "Update Profile", "User updated their own profile successfully");
        applicationEventPublisher.publishEvent(userUpdateEvent);

        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully."));
    }


    
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/{userId}/assignRoles")
    @Operation(summary = "Admins or super admins can assign roles")
    public ResponseEntity<ApiResponse> assignRolesToUser(
            @PathVariable Long userId,
            @RequestBody RoleAssignmentRequest roleAssignmentRequest,
            Authentication authentication) {

        userService.assignRolesToUser(userId, roleAssignmentRequest.getRoleNames());

        return ResponseEntity.ok(new ApiResponse(true, "Roles assigned successfully"));

    }    

    




    
    
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @Operation(summary = "Admins or super admins can delete roles")
    @DeleteMapping("/{userId}/deleteRole")
    public ResponseEntity<ApiResponse> deleteUserRole(
            @PathVariable Long userId,
            @RequestParam String roleName,
            Authentication authentication) {

        userService.deleteUserRole(userId, roleName, authentication);

        return ResponseEntity.ok(new ApiResponse(true, "Role removed successfully"));
    }

  
   
    
}
