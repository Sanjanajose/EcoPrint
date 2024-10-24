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
package com.ecoprint.printmanagement.model;

<<<<<<< HEAD
=======
import java.time.LocalDate;
>>>>>>> 982c1c6 (Initial commit)
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.NaturalId;

import com.ecoprint.printmanagement.model.audit.DateAudit;
import com.ecoprint.printmanagement.validation.annotation.NullOrNotBlank;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

<<<<<<< HEAD
@Table (name="users")
@Entity(name = "USER")
=======
@Entity(name = "USER")
@Table(name = "users")
>>>>>>> 982c1c6 (Initial commit)
public class User extends DateAudit {

    @Id
    @Column(name = "USER_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", allocationSize = 1)
    private Long id;

    @NaturalId
    @Column(name = "EMAIL", unique = true)
    @NotBlank(message = "User email cannot be null")
    private String email;

    @Column(name = "USERNAME", unique = true)
    @NullOrNotBlank(message = "Username can not be blank")
    private String username;

    @Column(name = "PASSWORD")
    @NotNull(message = "Password cannot be null")
    private String password;

    @Column(name = "FIRST_NAME")
    @NullOrNotBlank(message = "First name can not be blank")
    private String firstName;

    @Column(name = "LAST_NAME")
    @NullOrNotBlank(message = "Last name can not be blank")
    private String lastName;
<<<<<<< HEAD
=======
    
    @Column(name = "PHONE")
    private String phone; // New field

    @Column(name = "ADDRESS")
    private String address; // New field

    @Column(name = "GENDER")
    private String gender; // New field

    @Column(name = "COUNTRY")
    private String country; // New field

    @Column(name = "DOB") // Date of Birth
    private LocalDate dob; // New field

    @Column(name = "PROFILE_PICTURE")
    private String profilePicture; // URL or path to the profile picture
>>>>>>> 982c1c6 (Initial commit)

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean active;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "USER_AUTHORITY", joinColumns = {
            @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")}, inverseJoinColumns = {
            @JoinColumn(name = "ROLE_ID", referencedColumnName = "ROLE_ID")})
    private Set<Role> roles = new HashSet<>();

    @Column(name = "IS_EMAIL_VERIFIED", nullable = false)
    private Boolean isEmailVerified;

    public User() {
        super();
    }

    public User(User user) {
        id = user.getId();
        username = user.getUsername();
        password = user.getPassword();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        email = user.getEmail();
<<<<<<< HEAD
=======
        phone = user.getPhone(); // Copy new field
        address = user.getAddress(); // Copy new field
        gender = user.getGender(); // Copy new field
        country = user.getCountry(); // Copy new field
        dob = user.getDob(); // Copy new field
        profilePicture = user.getProfilePicture(); // Copy new field
>>>>>>> 982c1c6 (Initial commit)
        active = user.getActive();
        roles = user.getRoles();
        isEmailVerified = user.getEmailVerified();
    }
<<<<<<< HEAD
=======
    
    
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
>>>>>>> 982c1c6 (Initial commit)

    public void addRole(Role role) {
        roles.add(role);
        role.getUserList().add(this);
    }

    public void addRoles(Set<Role> roles) {
        roles.forEach(this::addRole);
    }

    public void removeRole(Role role) {
        roles.remove(role);
        role.getUserList().remove(this);
    }

    public void markVerificationConfirmed() {
        setEmailVerified(true);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> authorities) {
        roles = authorities;
    }

    public Boolean getEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", email='" + email + '\'' + ", username='" + username + '\'' + ", password='"
<<<<<<< HEAD
                + password + '\'' + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", active="
                + active + ", roles=" + roles + ", isEmailVerified=" + isEmailVerified + '}';
    }
=======
                + password + '\'' + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + 
                ", phone='" + phone + '\'' + ", address='" + address + '\'' + ", gender='" + gender + '\'' + 
                ", country='" + country + '\'' + ", dob=" + dob + ", profilePicture='" + profilePicture + '\'' + 
                ", active=" + active + ", roles=" + roles + ", isEmailVerified=" + isEmailVerified + '}';
    }

>>>>>>> 982c1c6 (Initial commit)
}
