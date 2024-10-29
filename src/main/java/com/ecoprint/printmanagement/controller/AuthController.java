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
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecoprint.printmanagement.event.*;
import com.ecoprint.printmanagement.exception.*;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.payload.*;
import com.ecoprint.printmanagement.model.token.EmailVerificationToken;
import com.ecoprint.printmanagement.model.token.RefreshToken;
import com.ecoprint.printmanagement.security.JwtTokenProvider;
import com.ecoprint.printmanagement.service.AuthService;
import com.ecoprint.printmanagement.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping("/login")
    @Operation(summary = "Logs the user in to the system and returns the auth tokens")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authService.authenticateUser(loginRequest)
                .orElseThrow(() -> new UserLoginException("Couldn't login user [" + loginRequest + "]"));

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authService.createAndPersistRefreshTokenForDevice(authentication, loginRequest)
                .map(RefreshToken::getToken)
                .map(refreshToken -> {
                    String jwtToken = authService.generateToken(customUserDetails);
                    return ResponseEntity.ok(new JwtAuthenticationResponse(jwtToken, refreshToken, tokenProvider.getExpiryDuration()));
                })
                .orElseThrow(() -> new UserLoginException("Couldn't create refresh token for: [" + loginRequest + "]"));
    }

    @PostMapping("/register")
    @Operation(summary = "Registers the user and publishes an event to generate the email verification")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        return authService.registerUser(registrationRequest)
                .map(user -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/auth/registrationConfirmation");
                    OnUserRegistrationCompleteEvent onUserRegistrationCompleteEvent =
                            new OnUserRegistrationCompleteEvent(user, urlBuilder);
                    applicationEventPublisher.publishEvent(onUserRegistrationCompleteEvent);
                    return ResponseEntity.ok(new ApiResponse(true, "User registered successfully. Check your email for verification."));
                })
                .orElseThrow(() -> new UserRegistrationException(registrationRequest.getEmail(), "Missing user object in database"));
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
                    OnUserAccountChangeEvent onPasswordChangeEvent = new OnUserAccountChangeEvent(changedUser, "Reset Password", "Changed Successfully");
                    applicationEventPublisher.publishEvent(onPasswordChangeEvent);
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

    @PostMapping("/refresh")
    @Operation(summary = "Refresh the expired JWT authentication by issuing a token refresh request and returns the updated response tokens")
    public ResponseEntity<JwtAuthenticationResponse> refreshJwtToken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
        return authService.refreshJwtToken(tokenRefreshRequest)
                .map(updatedToken -> {
                    String refreshToken = tokenRefreshRequest.getRefreshToken();
                    return ResponseEntity.ok(new JwtAuthenticationResponse(updatedToken, refreshToken, tokenProvider.getExpiryDuration()));
                })
                .orElseThrow(() -> new TokenRefreshException(tokenRefreshRequest.getRefreshToken(), "Unexpected error during token refresh. Please logout and login again."));
    }
}
