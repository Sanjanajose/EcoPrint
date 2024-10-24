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
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(name = "Registration Request", description = "The registration request payload")
public class RegistrationRequest {

    @NullOrNotBlank(message = "Registration username can be null but not blank")
    @Schema(name = "A valid username", allowableValues = "NonEmpty String")
    private String username;

    @NullOrNotBlank(message = "Registration email can be null but not blank")
    @Schema(name = "A valid email", required = true, allowableValues = "NonEmpty String")
    private String email;

    @NotNull(message = "Registration password cannot be null")
    @Schema(name = "A valid password string", required = true, allowableValues = "NonEmpty String")
    private String password;

    @NotNull(message = "Specify whether the user has to be registered as an admin or not")
    @Schema(name = "Flag denoting whether the user is an admin or not", required = true,
            type = "boolean", allowableValues = "true, false")
    private Boolean registerAsAdmin;

    // New fields
    @NullOrNotBlank(message = "Phone number can be null but not blank")
    @Schema(name = "Phone number", allowableValues = "NonEmpty String")
    private String phone;

    @NullOrNotBlank(message = "Address can be null but not blank")
    @Schema(name = "Address", allowableValues = "NonEmpty String")
    private String address;

    @Schema(name = "Gender", allowableValues = "NonEmpty String")
    private String gender;

    @Schema(name = "Country", allowableValues = "NonEmpty String")
    private String country;

    @Schema(name = "Date of Birth")
    private LocalDate dob;

    @Schema(name = "Profile Picture", description = "URL of the profile picture")
    private String profilePicture;

    // Constructors
    public RegistrationRequest(String username, String email, String password, Boolean registerAsAdmin,
                               String phone, String address, String gender, String country, LocalDate dob, String profilePicture) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.registerAsAdmin = registerAsAdmin;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.country = country;
        this.dob = dob;
        this.profilePicture = profilePicture;
    }

    public RegistrationRequest() {
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRegisterAsAdmin() {
        return registerAsAdmin;
    }

    public void setRegisterAsAdmin(Boolean registerAsAdmin) {
        this.registerAsAdmin = registerAsAdmin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
