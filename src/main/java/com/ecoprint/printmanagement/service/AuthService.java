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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.ecoprint.printmanagement.model.payload.LoginRequest;
import com.ecoprint.printmanagement.model.payload.PasswordResetLinkRequest;
import com.ecoprint.printmanagement.model.payload.PasswordResetRequest;
import com.ecoprint.printmanagement.model.payload.RegistrationRequest;
import com.ecoprint.printmanagement.model.payload.TokenRefreshRequest;
import com.ecoprint.printmanagement.model.payload.UpdatePasswordRequest;
import com.ecoprint.printmanagement.model.token.EmailVerificationToken;
import com.ecoprint.printmanagement.model.token.RefreshToken;
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
    public Optional<User> registerUser(RegistrationRequest newRegistrationRequest) {
        String newRegistrationRequestEmail = newRegistrationRequest.getEmail();
        if (emailAlreadyExists(newRegistrationRequestEmail)) {
            throw new ResourceAlreadyInUseException("Email", "Address", newRegistrationRequestEmail);
        }
        User newUser = userService.createUser(newRegistrationRequest);
        User registeredNewUser = userService.save(newUser);

        // Log the registration action
        activityLogService.logAction("User registered", registeredNewUser.getUsername(), registeredNewUser.getId(),
            "User registered with email: " + registeredNewUser.getEmail());

        return Optional.ofNullable(registeredNewUser);
    }

    public Boolean emailAlreadyExists(String email) {
        return userService.existsByEmail(email);
    }

    public Boolean usernameAlreadyExists(String username) {
        return userService.existsByUsername(username);
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
        activityLogService.logAction("Email verified", registeredUser.getUsername(), registeredUser.getId(),
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

    public Optional<User> updatePassword(CustomUserDetails customUserDetails, UpdatePasswordRequest updatePasswordRequest) {
        String email = customUserDetails.getEmail();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new UpdatePasswordException(email, "No matching user found"));

        if (!currentPasswordMatches(currentUser, updatePasswordRequest.getOldPassword())) {
            throw new UpdatePasswordException(currentUser.getEmail(), "Invalid current password");
        }
        String newPassword = passwordEncoder.encode(updatePasswordRequest.getNewPassword());
        currentUser.setPassword(newPassword);
        userService.save(currentUser);

        // Log the password update action
        activityLogService.logAction("Password updated", currentUser.getUsername(), currentUser.getId(),
            "User updated their password");

        return Optional.of(currentUser);
    }

    public String generateToken(CustomUserDetails customUserDetails) {
    	
        return tokenProvider.generateToken(customUserDetails);
    }
    
    public Optional<Authentication> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = customUserDetails.getUser();

        // Generate the JWT token using the "remember me" flag
        String token = tokenProvider.generateToken(customUserDetails, loginRequest.isRememberMe());

        // Log the login action
        activityLogService.logAction("User logged in", user.getUsername(), user.getId(),
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
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Missing refresh token in database. Please login again"));
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
                    activityLogService.logAction("Password reset", user.getUsername(), user.getId(),
                        "User reset their password using a reset token.");

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
            activityLogService.logAction("Temporary password sent", currentUser.getUsername(), currentUser.getId(),
                "User was sent a temporary password.");
        } catch (IOException | TemplateException | MessagingException e) {
            throw new MailSendException(email, "Password Reset");
        }

        return currentUser;
    }
}
