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

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.exception.MailSendException;
import com.ecoprint.printmanagement.exception.PasswordResetLinkException;
import com.ecoprint.printmanagement.exception.ResourceAlreadyInUseException;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.exception.SetAdminAccessException;
import com.ecoprint.printmanagement.exception.TokenRefreshException;
import com.ecoprint.printmanagement.exception.UpdatePasswordException;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.PasswordResetToken;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.UserDevice;
import com.ecoprint.printmanagement.model.UserNotificationPreferences;
import com.ecoprint.printmanagement.model.payload.LoginRequest;
import com.ecoprint.printmanagement.model.payload.PasswordResetLinkRequest;
import com.ecoprint.printmanagement.model.payload.PasswordResetRequest;
import com.ecoprint.printmanagement.model.payload.RegistrationRequest;
import com.ecoprint.printmanagement.model.payload.TokenRefreshRequest;
import com.ecoprint.printmanagement.model.payload.UpdatePasswordRequest;
import com.ecoprint.printmanagement.model.token.EmailVerificationToken;
import com.ecoprint.printmanagement.model.token.RefreshToken;
import com.ecoprint.printmanagement.repository.UserNotificationPreferencesRepository;
import com.ecoprint.printmanagement.repository.UserRepository;
import com.ecoprint.printmanagement.security.JwtTokenProvider;
import com.ecoprint.printmanagement.service.ActivityLogService;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final UserDeviceService userDeviceService;
    private final PasswordResetTokenService passwordResetService;
    private final MailService mailService;
    private final ActivityLogService activityLogService;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    UserNotificationPreferencesRepository usernotificationpreferencerepository;

    @Autowired
    public AuthService(UserService userService, JwtTokenProvider tokenProvider,
                       RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, EmailVerificationTokenService emailVerificationTokenService,
                       UserDeviceService userDeviceService, PasswordResetTokenService passwordResetService,
                       MailService mailService, ActivityLogService activityLogService) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.userDeviceService = userDeviceService;
        this.passwordResetService = passwordResetService;
        this.mailService = mailService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public Optional<User> registerUser(RegistrationRequest newRegistrationRequest, MultipartFile profilePicture) {
        String newRegistrationRequestEmail = newRegistrationRequest.getEmail();
        if (emailAlreadyExists(newRegistrationRequestEmail)) {
            throw new ResourceAlreadyInUseException("Email", "Address", newRegistrationRequestEmail);
        }
        User newUser = userService.createUser(newRegistrationRequest, profilePicture);
        User registeredNewUser = userService.save(newUser);
        
     // Create default notification preferences
        createDefaultNotificationPreferences(registeredNewUser);


        // Log the registration action
        activityLogService.logActivity("User registered", registeredNewUser.getUsername(), registeredNewUser.getId(),
            "User registered with email: " + registeredNewUser.getEmail());

        return Optional.ofNullable(registeredNewUser);
    }

    public Boolean emailAlreadyExists(String email) {
        return userService.existsByEmail(email);
    }

    public Boolean usernameAlreadyExists(String username) {
        return userService.existsByUsername(username);
    }
    
    
    
 // Method to create default notification preferences for a user
    private void createDefaultNotificationPreferences(User user) {
        UserNotificationPreferences preferences = new UserNotificationPreferences();
        preferences.setUser(user); // Assuming you have a relationship with the User entity
        preferences.setPreferEmail(true); // Default value
        preferences.setPreferInApp(true); // Default value
        preferences.setJobCompletedNotificationEnabled(true); // Default value
        preferences.setJobFailedNotificationEnabled(true); // Default value

        
        usernotificationpreferencerepository.save(preferences);
    }
    
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
        
    
    /**
     * Validates user credentials and returns user if valid.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return User if credentials are valid; null otherwise.
     */
    public User validateUserCredentials(String identifier, String password) {
        User user;
        if (identifier.contains("@")) { // Simple check to determine if it's an email
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", identifier));
        } else {
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", identifier));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username/email or password");
        }
        return user;
    }





    public Optional<User> confirmEmailRegistration(String emailToken) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(emailToken)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "Email verification", emailToken));

        User registeredUser = emailVerificationToken.getUser();
        if (registeredUser.getEmailVerified()) {
            return Optional.of(registeredUser);
        }

        emailVerificationTokenService.verifyExpiration(emailVerificationToken);
        emailVerificationToken.setConfirmedStatus();
        emailVerificationTokenService.save(emailVerificationToken);

        registeredUser.markVerificationConfirmed();
        userService.save(registeredUser);

        // Log the email verification action
        activityLogService.logActivity("Email verified", registeredUser.getUsername(), registeredUser.getId(),
            "User verified their email address.");

        return Optional.of(registeredUser);
    }

    public Optional<EmailVerificationToken> recreateRegistrationToken(String existingToken) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(existingToken)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "Existing email verification", existingToken));

        if (emailVerificationToken.getUser().getEmailVerified()) {
            return Optional.empty();
        }
        return Optional.ofNullable(emailVerificationTokenService.updateExistingTokenWithNameAndExpiry(emailVerificationToken));
    }

    private Boolean currentPasswordMatches(User currentUser, String password) {
        return passwordEncoder.matches(password, currentUser.getPassword());
    }

    /**public Optional<User> updatePassword(CustomUserDetails customUserDetails, UpdatePasswordRequest updatePasswordRequest) {
        if (customUserDetails == null) {
            throw new UpdatePasswordException("--Empty--", "User details not available in context");
        }

        String email = customUserDetails.getEmail();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new UpdatePasswordException(email, "No matching user found"));

        if (!currentPasswordMatches(currentUser, updatePasswordRequest.getOldPassword())) {
            throw new UpdatePasswordException(currentUser.getEmail(), "Invalid current password");
        }

        String newPassword = passwordEncoder.encode(updatePasswordRequest.getNewPassword());
        currentUser.setPassword(newPassword);
        userService.save(currentUser);
        return Optional.of(currentUser);
    } **/
    
    public Optional<User> updatePassword(CustomUserDetails customUserDetails, UpdatePasswordRequest updatePasswordRequest) {
        if (customUserDetails == null) {
            System.out.println("Error: customUserDetails is null. No authentication details available in the SecurityContext.");
            throw new UpdatePasswordException("--Empty--", "User details not available in context");
        }

        // Check if the object is an instance of CustomUserDetails
        if (!(customUserDetails instanceof CustomUserDetails)) {
            System.out.println("Warning: Retrieved UserDetails is not an instance of CustomUserDetails.");
        } else {
            System.out.println("Success: Retrieved instance of CustomUserDetails from SecurityContext.");
        }

        // Proceed with your password update logic
        String email = customUserDetails.getEmail();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new UpdatePasswordException(email, "No matching user found"));

        if (!currentPasswordMatches(currentUser, updatePasswordRequest.getOldPassword())) {
            throw new UpdatePasswordException(currentUser.getEmail(), "Invalid current password");
        }

        String newPassword = passwordEncoder.encode(updatePasswordRequest.getNewPassword());
        currentUser.setPassword(newPassword);
        userService.save(currentUser);
        return Optional.of(currentUser);
    }




    public String generateToken(CustomUserDetails customUserDetails) {
    	
        return tokenProvider.generateToken(customUserDetails);
    }
    
    public Optional<Authentication> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getIdentifier(), loginRequest.getPassword())
        );

        
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        

        // Generate the JWT token using the "remember me" flag
        String token = tokenProvider.generateToken(customUserDetails, loginRequest.isRememberMe());

        // Log the login action
        activityLogService.logActivity("User logged in", customUserDetails.getUsername(), customUserDetails.getId(),
            "User logged in from device: " + loginRequest.getDeviceInfo().getDeviceType());

        // Return the authentication wrapped in an Optional
        return Optional.of(authentication);
    }





    private String generateTokenFromUserId(Long userId) {
        return tokenProvider.generateTokenFromUserId(userId);
    }

    public Optional<RefreshToken> createAndPersistRefreshTokenForDevice(Authentication authentication, LoginRequest loginRequest) {
        User currentUser = (User) authentication.getPrincipal();
        String deviceId = loginRequest.getDeviceInfo().getDeviceId();
        userDeviceService.findDeviceByUserId(currentUser.getId(), deviceId)
                .map(UserDevice::getRefreshToken)
                .map(RefreshToken::getId)
                .ifPresent(refreshTokenService::deleteById);

        UserDevice userDevice = userDeviceService.createUserDevice(loginRequest.getDeviceInfo());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken();
        userDevice.setUser(currentUser);
        userDevice.setRefreshToken(refreshToken);
        refreshToken.setUserDevice(userDevice);
        refreshToken = refreshTokenService.save(refreshToken);
        return Optional.ofNullable(refreshToken);
    }

    
    
    
    /**
     * Refresh the expired jwt token using a refresh token and device info. The
     * * refresh token is mapped to a specific device and if it is unexpired, can help
     * * generate a new jwt. If the refresh token is inactive for a device or it is expired,
     * * throw appropriate errors.
     */
    public Optional<String> refreshJwtToken(TokenRefreshRequest tokenRefreshRequest) {
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

        return Optional.of(refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshToken -> {
                    refreshTokenService.verifyExpiration(refreshToken);
                    userDeviceService.verifyRefreshAvailability(refreshToken);
                    refreshTokenService.increaseCount(refreshToken);
                    return refreshToken;
                })
                .map(RefreshToken::getUserDevice)
                .map(UserDevice::getUser)
                .map(CustomUserDetails::new)
                .map(this::generateToken))
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Missing refresh token in database.Please login again"));
    }



    public Optional<PasswordResetToken> generatePasswordResetToken(PasswordResetLinkRequest passwordResetLinkRequest) {
        String email = passwordResetLinkRequest.getEmail();
        return userService.findByEmail(email)
                .map(passwordResetService::createToken)
                .orElseThrow(() -> new PasswordResetLinkException(email, "No matching user found for the given request"));
    }

    public Optional<User> resetPassword(PasswordResetRequest request) {
        PasswordResetToken token = passwordResetService.getValidToken(request);
        final String encodedPassword = passwordEncoder.encode(request.getConfirmPassword());

        return Optional.of(token)
                .map(passwordResetService::claimToken)
                .map(PasswordResetToken::getUser)
                .map(user -> {
                    user.setPassword(encodedPassword);
                    userService.save(user);

                    // Log the password reset action
                    activityLogService.logActivity("Password reset", user.getUsername(), user.getId(),
                        "User reset their password using a reset token.");

                    // Delete the reset token after use
                    passwordResetService.deleteToken(token);

                    return user;
                });
    } 
    
    
    
    

    public User resetWithTempPassword(String email) {
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        final String encodedPassword = passwordEncoder.encode(tempPassword);
        
        User currentUser = userService.findByEmail(email)
            .orElseThrow(() -> new SetAdminAccessException(email, "No Matching User Found"));
        
        currentUser.setPassword(encodedPassword);
        userService.save(currentUser);
        
        try {
            mailService.sendTempPassword(tempPassword, email);

            // Log the temporary password reset action
            activityLogService.logActivity("Temporary password sent", currentUser.getUsername(), currentUser.getId(),
                "User was sent a temporary password.");
        } catch (IOException | TemplateException | MessagingException e) {
            throw new MailSendException(email, "Password Reset");
        }

        return currentUser;
    }
}
