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
package com.ecoprint.printmanagement.model.payload;

import com.ecoprint.printmanagement.validation.annotation.NullOrNotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Login Request", description = "The login request payload")
public class LoginRequest {

    @NullOrNotBlank(message = "Identifier can be null but not blank")
    @Schema(name = "Username or Email", required = true, allowableValues = "NonEmpty String", description = "The user identifier, can be either username or email")
    private String identifier; // Accepts either username or email

    @NotNull(message = "Login password cannot be blank")
    @Schema(name = "Valid user password", required = true, allowableValues = "NonEmpty String")
    private String password;

    @Valid
    @NotNull(message = "Device info cannot be null")
    @Schema(name = "Device info", required = true, type = "object", allowableValues = "A valid deviceInfo object")
    private DeviceInfo deviceInfo;

    @Schema(name = "OTP", description = "Optional field for two-factor authentication")
    private String otp; // Optional field for 2FA

    private boolean rememberMe;

    // Constructor with all fields
    public LoginRequest(String identifier, String password, DeviceInfo deviceInfo, String otp, boolean rememberMe) {
        this.identifier = identifier;
        this.password = password;
        this.deviceInfo = deviceInfo;
        this.otp = otp;
        this.rememberMe = rememberMe;
    }

    // Default constructor
    public LoginRequest() {
    }

    // Getters and Setters
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
