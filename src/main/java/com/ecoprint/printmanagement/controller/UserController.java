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

<<<<<<< HEAD
import org.slf4j.Logger;
=======
>>>>>>> 982c1c6 (Initial commit)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
<<<<<<< HEAD
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
=======
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
>>>>>>> 982c1c6 (Initial commit)

import com.ecoprint.printmanagement.annotation.CurrentUser;
import com.ecoprint.printmanagement.event.OnUserAccountChangeEvent;
import com.ecoprint.printmanagement.event.OnUserLogoutSuccessEvent;
<<<<<<< HEAD
import com.ecoprint.printmanagement.exception.UpdatePasswordException;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.payload.ApiResponse;
import com.ecoprint.printmanagement.model.payload.LogOutRequest;
import com.ecoprint.printmanagement.model.payload.UpdatePasswordRequest;
=======
import com.ecoprint.printmanagement.exception.FileUploadException;
import com.ecoprint.printmanagement.exception.UpdatePasswordException;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.payload.ApiResponse;
import com.ecoprint.printmanagement.model.payload.LogOutRequest;
import com.ecoprint.printmanagement.model.payload.UpdatePasswordRequest;
import com.ecoprint.printmanagement.model.payload.UserUpdateRequest;
>>>>>>> 982c1c6 (Initial commit)
import com.ecoprint.printmanagement.service.AuthService;
import com.ecoprint.printmanagement.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

<<<<<<< HEAD
@RestController
@RequestMapping("/api/user")
@Tag(name = "User Rest API", description = "Defines endpoints for the logged in user. It's secured by default")

public class UserController {

//    private static final Logger logger = Logger.getLogger(UserController.class);

    private final AuthService authService;

    private final UserService userService;

=======
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Rest API", description = "Defines endpoints for the logged-in user. It's secured by default")
public class UserController {

    private final AuthService authService;
    private final UserService userService;
>>>>>>> 982c1c6 (Initial commit)
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public UserController(AuthService authService, UserService userService, ApplicationEventPublisher applicationEventPublisher) {
        this.authService = authService;
        this.userService = userService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
<<<<<<< HEAD
     * Gets the current user profile of the logged in user
=======
     * Gets the current user profile of the logged-in user
>>>>>>> 982c1c6 (Initial commit)
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Returns the current user profile")
<<<<<<< HEAD
    public ResponseEntity getUserProfile(@CurrentUser CustomUserDetails currentUser) {
//        logger.info(currentUser.getEmail() + " has role: " + currentUser.getRoles());
=======
    public ResponseEntity<?> getUserProfile(@CurrentUser CustomUserDetails currentUser) {
>>>>>>> 982c1c6 (Initial commit)
        return ResponseEntity.ok("Hello. This is about me");
    }

    /**
     * Returns all admins in the system. Requires Admin access
     */
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Returns the list of configured admins. Requires ADMIN Access")
<<<<<<< HEAD
    public ResponseEntity getAllAdmins() {
//        logger.info("Inside secured resource with admin");
=======
    public ResponseEntity<?> getAllAdmins() {
>>>>>>> 982c1c6 (Initial commit)
        return ResponseEntity.ok("Hello. This is about admins");
    }

    /**
<<<<<<< HEAD
     * Updates the password of the current logged in user
     */
    @PostMapping("/password/update")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Allows the user to change his password once logged in by supplying the correct current " +
            "password")
    public ResponseEntity updateUserPassword(@CurrentUser CustomUserDetails customUserDetails,
                                             @Param(value = "The UpdatePasswordRequest payload") @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
=======
     * Updates the password of the current logged-in user
     */
    @PostMapping("/password/update")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Allows the user to change his password once logged in by supplying the correct current password")
    public ResponseEntity<?> updateUserPassword(@CurrentUser CustomUserDetails customUserDetails,
                                             @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
>>>>>>> 982c1c6 (Initial commit)

        return authService.updatePassword(customUserDetails, updatePasswordRequest)
                .map(updatedUser -> {
                    OnUserAccountChangeEvent onUserPasswordChangeEvent = new OnUserAccountChangeEvent(updatedUser,
                            "Update Password", "Change successful");
                    applicationEventPublisher.publishEvent(onUserPasswordChangeEvent);
                    return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
                })
                .orElseThrow(() -> new UpdatePasswordException("--Empty--", "No such user present."));
    }

    /**
     * Log the user out from the app/device. Release the refresh token associated with the
     * user device.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logs the specified user device and clears the refresh tokens associated with it")
<<<<<<< HEAD
    public ResponseEntity logoutUser(@CurrentUser CustomUserDetails customUserDetails,
                                     @Param(value = "The LogOutRequest payload") @Valid @RequestBody LogOutRequest logOutRequest) {
        userService.logoutUser(customUserDetails, logOutRequest);
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();

        OnUserLogoutSuccessEvent logoutSuccessEvent = new OnUserLogoutSuccessEvent(customUserDetails.getEmail(),
                credentials.toString(), logOutRequest);
        applicationEventPublisher.publishEvent(logoutSuccessEvent);
        return ResponseEntity.ok(new ApiResponse(true, "Log out successful"));
    }
    
    @GetMapping("/resetPassword")
    @Operation(summary = "Reset Password with a temporary password for given user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity resetPassword(@RequestParam String email) {
    	authService.resetWithTempPassword(email);
    	return ResponseEntity.ok(new ApiResponse(true, "Temporary password set successfully"));
    }
    
    @GetMapping("/setAdminAccess")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity setAdminAccess(@RequestParam String email) {
    	userService.setAdminAccess(email);
    	return ResponseEntity.ok(new ApiResponse(true, "Admin Access Given Successfully"));
    }
=======
    public ResponseEntity<?> logoutUser(@CurrentUser CustomUserDetails customUserDetails,
                                     @Valid @RequestBody LogOutRequest logOutRequest) {
        userService.logoutUser(customUserDetails, logOutRequest);
        OnUserLogoutSuccessEvent logoutSuccessEvent = new OnUserLogoutSuccessEvent(customUserDetails.getEmail(),
                SecurityContextHolder.getContext().getAuthentication().getCredentials().toString(), logOutRequest);
        applicationEventPublisher.publishEvent(logoutSuccessEvent);
        return ResponseEntity.ok(new ApiResponse(true, "Log out successful"));
    }

    /**
     * Resets a user's password with a temporary password. Only admins can perform this action.
     */
    @GetMapping("/resetPassword")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset Password with a temporary password for the given user")
    public ResponseEntity<?> resetPassword(@RequestParam String email) {
        authService.resetWithTempPassword(email);
        return ResponseEntity.ok(new ApiResponse(true, "Temporary password set successfully"));
    }

    /**
     * Set admin access for the specified user. Requires Super Admin privileges.
     */
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
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestPart("user") UserUpdateRequest updateRequest,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        Long userId = currentUser.getId();

        // Validate that the email isn't already in use by another user
        if (userService.existsByEmail(updateRequest.getEmail()) && !currentUser.getEmail().equals(updateRequest.getEmail())) {
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
            } catch (FileUploadException e) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
            }
        }

        // Prevent the user from updating their own roles
        Set<String> currentRoles = currentUser.getAuthorities().stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.toSet());

        // Update user details
        User updatedUserData = new User();
        updatedUserData.setUsername(updateRequest.getUsername());
        updatedUserData.setEmail(updateRequest.getEmail());
        updatedUserData.setPhone(updateRequest.getPhone());
        updatedUserData.setAddress(updateRequest.getAddress());
        updatedUserData.setGender(updateRequest.getGender());
        updatedUserData.setCountry(updateRequest.getCountry());
        updatedUserData.setDob(updateRequest.getDob());

        // Set updated profile picture if available
        if (updateRequest.getProfilePicture() != null) {
            updatedUserData.setProfilePicture(updateRequest.getProfilePicture());
        }

        // Update user in the service layer
        User updatedUser = userService.updateUser(userId, updatedUserData, currentRoles);

        // Trigger user account change event
        OnUserAccountChangeEvent userUpdateEvent = new OnUserAccountChangeEvent(updatedUser, 
            "Update Profile", "User updated their own profile successfully");
        applicationEventPublisher.publishEvent(userUpdateEvent);

        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully."));
    }

>>>>>>> 982c1c6 (Initial commit)
}
