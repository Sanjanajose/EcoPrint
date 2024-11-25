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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecoprint.printmanagement.event.OnGenerateResetLinkEvent;
import com.ecoprint.printmanagement.event.OnRegenerateEmailVerificationEvent;
import com.ecoprint.printmanagement.event.OnUserAccountChangeEvent;
import com.ecoprint.printmanagement.event.OnUserRegistrationCompleteEvent;
import com.ecoprint.printmanagement.exception.InvalidTokenRequestException;
import com.ecoprint.printmanagement.exception.PasswordResetException;
import com.ecoprint.printmanagement.exception.PasswordResetLinkException;
import com.ecoprint.printmanagement.exception.TokenRefreshException;
import com.ecoprint.printmanagement.exception.UserLoginException;
import com.ecoprint.printmanagement.exception.UserRegistrationException;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.LoginRequest2FA;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.payload.ApiResponse;
import com.ecoprint.printmanagement.model.payload.JwtAuthenticationResponse;
import com.ecoprint.printmanagement.model.payload.LoginRequest;
import com.ecoprint.printmanagement.model.payload.PasswordResetLinkRequest;
import com.ecoprint.printmanagement.model.payload.PasswordResetRequest;
import com.ecoprint.printmanagement.model.payload.RegistrationRequest;
import com.ecoprint.printmanagement.model.payload.TokenRefreshRequest;
import com.ecoprint.printmanagement.model.token.EmailVerificationToken;
import com.ecoprint.printmanagement.model.token.RefreshToken;
import com.ecoprint.printmanagement.security.JwtTokenProvider;
import com.ecoprint.printmanagement.service.AuthService;
import com.ecoprint.printmanagement.service.MailService;
import com.ecoprint.printmanagement.service.OTPService;
import com.ecoprint.printmanagement.service.UserActivityService;
import com.ecoprint.printmanagement.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authorization Rest API", description = "Defines endpoints that can be hit only when the user is not logged in. It's not secured by default.")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserService userService; 
    

    @Autowired
    private UserActivityService userActivityService;

    @Autowired 
    private MailService mailService;
    
    @Autowired 
    private OTPService otpService;



    @Autowired
    public AuthController(AuthService authService, JwtTokenProvider tokenProvider, 
                          ApplicationEventPublisher applicationEventPublisher, UserService userService) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
        this.applicationEventPublisher = applicationEventPublisher;
        this.userService = userService;
    }

    @Operation(summary = "Checks if the given email is in use")
    @GetMapping("/checkEmailInUse")
    public ResponseEntity<ApiResponse> checkEmailInUse(@RequestParam("email") String email) {
        Boolean emailExists = authService.emailAlreadyExists(email);
        return ResponseEntity.ok(new ApiResponse(true, emailExists.toString()));
    }

    @Operation(summary = "Checks if the given username is in use")
    @GetMapping("/checkUsernameInUse")
    public ResponseEntity<ApiResponse> checkUsernameInUse(@RequestParam("username") String username) {
        Boolean usernameExists = authService.usernameAlreadyExists(username);
        return ResponseEntity.ok(new ApiResponse(true, usernameExists.toString()));
    }
    
   /* @Operation(summary = "Register a new user", description = "User registration with profile picture upload")
    @PostMapping("/registers")
    public ResponseEntity<User> registerUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("profilePicture") @Schema(description = "Profile picture to upload", type = "string", format = "binary") MultipartFile profilePicture) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);  // In real applications, you should hash the password before storing
		return null;

    } */
    
    
    @PostMapping("/register")
    @Operation(summary = "Registers the user and publishes an event to generate the email verification")
    public ResponseEntity<ApiResponse> registerUser(
    		@Valid @RequestParam("registrationRequest") String registrationRequest,
    		@RequestParam("profilePic") @io.swagger.v3.oas.annotations.media.Schema(description = "Profile picture to upload", type = "string", format = "binary") MultipartFile profilePic
    		) throws JsonMappingException, JsonProcessingException {
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.registerModule(new JavaTimeModule());
    	RegistrationRequest request = objectMapper.readValue(registrationRequest, RegistrationRequest.class);

        return authService.registerUser(request, profilePic)
                .map(user -> {
                    // Log registration activity
                    userActivityService.logUserActivity(user.getId(), "REGISTER", "User registered");

                    // Prepare the URL for email verification
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/auth/registrationConfirmation");

                    // Publish registration completion event
                    OnUserRegistrationCompleteEvent onUserRegistrationCompleteEvent =
                            new OnUserRegistrationCompleteEvent(user, urlBuilder);
                    applicationEventPublisher.publishEvent(onUserRegistrationCompleteEvent);

                    // Return success response
                    return ResponseEntity.ok(new ApiResponse(true, "User registered successfully. Check your email for verification."));
                })
                .orElseThrow(() -> new UserRegistrationException(request.getEmail(), "Missing user object in database"));
    }
    
    
    





    @PostMapping("/login")
    @Operation(summary = "Logs the user into the system and returns the auth tokens, with optional two-factor authentication (2FA) if enabled.")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Step 1: Authenticate the user
        User user = authService.validateUserCredentials(loginRequest.getIdentifier(), loginRequest.getPassword());
        CustomUserDetails customUserDetails = new CustomUserDetails(user); // Assuming you have a constructor for this
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Log the login activity
        Long userId = user.getId();
        userActivityService.logUserActivity(userId, "LOGIN", "User logged in");

        if (user.isTwoFactorEnabled()) {
            // Step 2: Handle 2FA
            if (loginRequest.getOtp() != null) {
                boolean isValidOtp = otpService.validateOTP(userId, loginRequest.getOtp());
                if (isValidOtp) {
                    // OTP is valid, proceed with generating tokens
                    return generateTokens(authentication, loginRequest);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Invalid or expired OTP. Please try again.");
                }
            } else {
                // Generate and send OTP if it’s not provided
                String otp = otpService.generateAndSaveOTP(userId);
                if ("email".equalsIgnoreCase(user.getPreferred2FAMethod())) {
                    mailService.sendOTPEmail(user.getEmail(), otp);
                } 
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("OTP sent. Please enter the OTP to complete login.");
            }
        }

        // Step 3: Normal login flow if 2FA is not enabled
        return authService.createAndPersistRefreshTokenForDevice(authentication, loginRequest)
                .map(RefreshToken::getToken)
                .map(refreshToken -> {
                    String jwtToken = authService.generateToken(customUserDetails);
                    return ResponseEntity.ok(new JwtAuthenticationResponse(jwtToken, refreshToken, tokenProvider.getExpiryDuration()));
                })
                .orElseThrow(() -> new UserLoginException("Couldn't create refresh token for: [" + loginRequest + "]"));
    }

    
    

    // Helper method to generate JWT and refresh tokens
    private ResponseEntity<JwtAuthenticationResponse> generateTokens(Authentication authentication, LoginRequest loginRequest) {
        // Generate JWT token and refresh token
        return authService.createAndPersistRefreshTokenForDevice(authentication, loginRequest)
                .map(RefreshToken::getToken)
                .map(refreshToken -> {
                    String jwtToken = authService.generateToken((CustomUserDetails) authentication.getPrincipal());
                    return ResponseEntity.ok(new JwtAuthenticationResponse(jwtToken, refreshToken, tokenProvider.getExpiryDuration()));
                })
                .orElseThrow(() -> new UserLoginException("Couldn't create refresh token for: [" + loginRequest + "]"));
    }




        
    

    @PostMapping("/password/resetlink")
    @Operation(summary = "Receive the reset link request and publish event to send mail containing the password reset link")
    public ResponseEntity<ApiResponse> resetLink(@Valid @RequestBody PasswordResetLinkRequest passwordResetLinkRequest) {
        return authService.generatePasswordResetToken(passwordResetLinkRequest)
                .map(passwordResetToken -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/auth/password/reset");
                    OnGenerateResetLinkEvent generateResetLinkMailEvent =
                            new OnGenerateResetLinkEvent(passwordResetToken, urlBuilder);
                    applicationEventPublisher.publishEvent(generateResetLinkMailEvent);
                    return ResponseEntity.ok(new ApiResponse(true, "Password reset link sent successfully"));
                })
                .orElseThrow(() -> new PasswordResetLinkException(passwordResetLinkRequest.getEmail(), "Couldn't create a valid token"));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset the password after verification and publish an event to send the acknowledgement email")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        return authService.resetPassword(passwordResetRequest)
                .map(changedUser -> {
                    // Log password reset activity
                    userActivityService.logUserActivity(changedUser.getId(), "PASSWORD_RESET", "Password changed successfully");

                    // Publish an account change event for password reset
                    OnUserAccountChangeEvent onPasswordChangeEvent = new OnUserAccountChangeEvent(
                            changedUser, "Reset Password", "Changed Successfully");
                    applicationEventPublisher.publishEvent(onPasswordChangeEvent);

                    // Return success response
                    return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
                })
                .orElseThrow(() -> new PasswordResetException(passwordResetRequest.getToken(), "Error in resetting password"));
    }


    @GetMapping("/registrationConfirmation")
    @Operation(summary = "Confirms the email verification token that has been generated for the user during registration")
    public ResponseEntity<ApiResponse> confirmRegistration(@RequestParam("token") String token) {
        return authService.confirmEmailRegistration(token)
                .map(user -> ResponseEntity.ok(new ApiResponse(true, "User verified successfully")))
                .orElseThrow(() -> new InvalidTokenRequestException("Email Verification Token", token, "Failed to confirm. Please generate a new email verification request"));
    }

    @GetMapping("/resendRegistrationToken")
    @Operation(summary = "Resend the email registration with an updated token expiry.")
    public ResponseEntity<ApiResponse> resendRegistrationToken(@RequestParam("token") String existingToken) {
        EmailVerificationToken newEmailToken = authService.recreateRegistrationToken(existingToken)
                .orElseThrow(() -> new InvalidTokenRequestException("Email Verification Token", existingToken, "User is already registered. No need to re-generate token"));

        return Optional.ofNullable(newEmailToken.getUser())
                .map(registeredUser -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/auth/registrationConfirmation");
                    OnRegenerateEmailVerificationEvent regenerateEmailVerificationEvent = new OnRegenerateEmailVerificationEvent(registeredUser, urlBuilder, newEmailToken);
                    applicationEventPublisher.publishEvent(regenerateEmailVerificationEvent);
                    return ResponseEntity.ok(new ApiResponse(true, "Email verification resent successfully"));
                })
                .orElseThrow(() -> new InvalidTokenRequestException("Email Verification Token", existingToken, "No user associated with this request. Re-verification denied"));
    }

    /**
     * Refresh the expired jwt token using a refresh token for the specific device
     * and return a new token to the caller
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh the expired jwt authentication by issuing a token refresh request and returns the" +
            "updated response tokens")
    public ResponseEntity refreshJwtToken(@Param(value = "The TokenRefreshRequest payload") @Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {

        return authService.refreshJwtToken(tokenRefreshRequest)
                .map(updatedToken -> {
                    String refreshToken = tokenRefreshRequest.getRefreshToken();
//                    logger.info("Created new Jwt Auth token: " + updatedToken);
                    return ResponseEntity.ok(new JwtAuthenticationResponse(updatedToken, refreshToken, tokenProvider.getExpiryDuration()));
                })
                .orElseThrow(() -> new TokenRefreshException(tokenRefreshRequest.getRefreshToken(), "Unexpected error during token refresh. Please logout and login again."));
    }
}

