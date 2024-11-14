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
import org.springframework.security.core.Authentication;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.annotation.CurrentUser;
import com.ecoprint.printmanagement.config.RolePermissionMapping;
import com.ecoprint.printmanagement.exception.*;
import com.ecoprint.printmanagement.model.*;
import com.ecoprint.printmanagement.model.payload.LogOutRequest;
import com.ecoprint.printmanagement.model.payload.RegistrationRequest;
import com.ecoprint.printmanagement.model.payload.RoleAssignmentRequest;
import com.ecoprint.printmanagement.repository.RoleRepository;
import com.ecoprint.printmanagement.repository.UserDeviceRepository;
import com.ecoprint.printmanagement.repository.UserRepository;
import com.ecoprint.printmanagement.service.ActivityLogService;

import jakarta.transaction.Transactional;
import net.coobird.thumbnailator.Thumbnails;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {

	
	@PersistenceContext
    private EntityManager entityManager;  
	private final RoleRepository roleRepository;
	private final UserDeviceRepository userDeviceRepository;    
	private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserDeviceService userDeviceService;
    private final RefreshTokenService refreshTokenService;
    private final ActivityLogService activityLogService; // Added for logging
    private static final Set<String> ALLOWED_FILE_FORMATS = Set.of("jpg", "jpeg", "png");

    @Autowired
    public UserService(RoleRepository roleRepository, UserDeviceRepository userDeviceRepository, PasswordEncoder passwordEncoder, UserRepository userRepository, RoleService roleService,
                        UserDeviceService userDeviceService, RefreshTokenService refreshTokenService, ActivityLogService activityLogService) {
    	this.roleRepository = roleRepository;
    	this.userDeviceRepository = userDeviceRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.userDeviceService = userDeviceService;
        this.refreshTokenService = refreshTokenService;
        this.activityLogService = activityLogService; // Initialize ActivityLogService
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User createUser(RegistrationRequest registerRequest) {
        // Check for existing email and username
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceAlreadyInUseException("Email", "Address", registerRequest.getEmail());
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResourceAlreadyInUseException("Username", "Name", registerRequest.getUsername());
        }

        // Validate password strength
        validatePasswordStrength(registerRequest.getPassword());

        // Create new user instance
        User newUser = new User();
        Boolean isNewUserAsAdmin = registerRequest.getRegisterAsAdmin();

        // Ensure isNewUserAsAdmin is not null before using it
        if (isNewUserAsAdmin == null) {
            isNewUserAsAdmin = false; // Default to false if not provided
        }

        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setUsername(registerRequest.getUsername());

        // Assign roles based on isNewUserAsAdmin
        Set<Role> roles = getRolesForNewUser(isNewUserAsAdmin);
        newUser.setRoles(roles);

        // Map roles to permissions
        Set<Permission> rolePermissions = roles.stream()
                .flatMap(role -> RolePermissionMapping.rolePermissions.get(role.getRole()).stream())
                .collect(Collectors.toSet());
        newUser.setPermissions(rolePermissions);

        // Set other user details
        newUser.setActive(true);
        newUser.setEmailVerified(false);
        newUser.setPhone(registerRequest.getPhone());
        newUser.setAddress(registerRequest.getAddress());
        newUser.setGender(registerRequest.getGender());
        newUser.setCountry(registerRequest.getCountry());
        newUser.setDob(registerRequest.getDob());
        newUser.setProfilePicture(registerRequest.getProfilePicture());
        newUser.setTwoFactorEnabled(registerRequest.isTwoFactorEnabled());
        newUser.setPreferred2FAMethod(registerRequest.getPreferred2FAMethod());
        // Save the user to the repository
        User savedUser = userRepository.save(newUser);

        // Log user creation
        activityLogService.logActivity("User created", newUser.getUsername(), newUser.getId(), "New user registered.");

        return savedUser;
    }

    public void validatePasswordStrength(String password) {
        if (password.length() < 8 || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*()].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters long, contain a number, and a special character.");
        }
    }

    public User deleteUserRole(Long userId, String roleName, Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        if (!currentUserHasPermissionToAssign(authentication, roleName)) {
            throw new AccessDeniedException("You don't have permission to remove this role.");
        }

        // Remove the role and save
        user.getRoles().remove(role);
        userRepository.save(user);

        // Log role change activity
        activityLogService.logRoleChange(userId, roleName, "Role removed");

        // Update permissions
        // Update permissions with null check on RolePermissionMapping
        Set<Permission> updatedPermissions = user.getRoles().stream()
                .flatMap(r -> {
                    Set<Permission> permissions = RolePermissionMapping.rolePermissions.get(r.getRole());
                    return permissions != null ? permissions.stream() : Stream.empty();
                })
                .collect(Collectors.toSet());

        return userRepository.save(user);
    }


    private Set<Role> getRolesForNewUser(Boolean isAdmin) {
        Set<Role> roles = new HashSet<>();
        Role defaultRole = roleService.findByName("ROLE_USER")
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
        roles.add(defaultRole);

        // Safely check if isAdmin is true
        if (Boolean.TRUE.equals(isAdmin)) {
            Role adminRole = roleService.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_ADMIN"));
            roles.add(adminRole);
        }
        return roles;
    }


    /**
     * Updates a user's details, including roles, while performing necessary validations.
     */
    public User updateUser(Long userId, User updatedUserData, Set<String> roleNames) {
        // Find the existing user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update basic information
        user.setUsername(updatedUserData.getUsername());
        user.setEmail(updatedUserData.getEmail());
        user.setPhone(updatedUserData.getPhone());
        user.setAddress(updatedUserData.getAddress());
        user.setGender(updatedUserData.getGender());
        user.setCountry(updatedUserData.getCountry());
        user.setDob(updatedUserData.getDob());
        user.setProfilePicture(updatedUserData.getProfilePicture());

        // Check if the new email or username is unique
        if (userRepository.existsByEmail(updatedUserData.getEmail()) && !user.getEmail().equals(updatedUserData.getEmail())) {
            throw new ResourceAlreadyInUseException("Email", "Address", updatedUserData.getEmail());
        }

        if (userRepository.existsByUsername(updatedUserData.getUsername()) && !user.getUsername().equals(updatedUserData.getUsername())) {
            throw new ResourceAlreadyInUseException("Username", "Name", updatedUserData.getUsername());
        }

        // Validate and assign roles
        Set<Role> updatedRoles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleService.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

            // Ensure only users with appropriate permissions can assign admin roles
            if (role.isAdminRole() && !currentUserHasPermissionToAssignAdmin()) {
                throw new AccessDeniedException("You do not have permission to assign admin roles.");
            }
            updatedRoles.add(role);
        }

        // Assign the validated roles to the user
        user.setRoles(updatedRoles);

        // Save the updated user information
        return userRepository.save(user);
    }

    public User assignRolesToUser(Long userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // For each role name in the set, find the role and add it to the user
        Set<Role> rolesToAssign = roleNames.stream()
                .map(roleName -> roleService.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName)))
                .collect(Collectors.toSet());

        // Ensure permissions cascade correctly
        user.getRoles().addAll(rolesToAssign);
        user.setPermissions(
            user.getRoles().stream()
                .flatMap(role -> RolePermissionMapping.rolePermissions.get(role.getRole()).stream())
                .collect(Collectors.toSet())
        );

        userRepository.save(user);
        activityLogService.logRoleChange(userId, String.join(", ", roleNames), "Roles assigned");

        return user;
    }


    private boolean currentUserHasPermissionToAssign(Authentication authentication, String roleName) {
        // Example logic to verify if the current authenticated user can assign the role
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SUPERADMIN") ||
                                             (grantedAuthority.getAuthority().equals("ROLE_ADMIN") && !roleName.equals("ROLE_ADMIN")));
    }


    private boolean currentUserHasPermissionToAssignAdmin() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }


    public void logoutUser(@CurrentUser CustomUserDetails currentUser, LogOutRequest logOutRequest) {
        String deviceId = logOutRequest.getDeviceInfo().getDeviceId();
        UserDevice userDevice = userDeviceService.findDeviceByUserId(currentUser.getId(), deviceId)
            .filter(device -> device.getDeviceId().equals(deviceId))
            .orElseThrow(() -> new UserLogoutException(logOutRequest.getDeviceInfo().getDeviceId(), 
                "Invalid device Id supplied. No matching device found for the given user"));

        refreshTokenService.deleteById(userDevice.getRefreshToken().getId());
    }


    
    
    public void setAdminAccess(String email) {
        User currentUser = findByEmail(email)
            .orElseThrow(() -> new SetAdminAccessException(email, "No matching user found"));

        Set<Role> newUserRoles = new HashSet<>(roleService.findAllWithoutSuperAdmin());
        currentUser.setRoles(newUserRoles);
        userRepository.save(currentUser);
    }

    public String uploadProfilePicture(MultipartFile file, int width, int height) {
        long MAX_FILE_SIZE = 5 * 1024 * 1024;
        String uploadDir = "uploads/profile-pictures/";

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds the maximum limit of 5MB.");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName).toLowerCase();

        if (!ALLOWED_FILE_FORMATS.contains(fileExtension)) {
            throw new FileUploadException("Unsupported file format: " + fileExtension + ". Only JPG, JPEG, and PNG are allowed.");
        }

        String fileName = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            String outputFormat = fileExtension.equals("png") ? "png" : "jpg";

            Thumbnails.of(file.getInputStream())
                      .size(width, height)
                      .outputFormat(outputFormat)
                      .toFile(filePath.toFile());

            return "/uploads/profile-pictures/" + fileName;
        } catch (IOException e) {
            throw new FileUploadException("Could not store the file. Error: " + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    public boolean isSameUser(Long userId, String email) {
        Optional<User> userWithEmail = userRepository.findByEmail(email);
        return userWithEmail.map(user -> user.getId().equals(userId)).orElse(false);
    }

    public List<String> getAdminNotificationTokens() {
        // Fetch the admin role from RoleRepository
        Optional<Role> adminRoleOpt = roleRepository.findByRole(RoleName.ROLE_ADMIN);

        if (adminRoleOpt.isPresent()) {
            Role adminRole = adminRoleOpt.get();
            
            // Find users with the admin role and get their associated notification tokens from UserDevice
            return userRepository.findByRolesContaining(adminRole)
                                 .stream()
                                 .map(User::getId) // Get User IDs for admin users
                                 .flatMap(userId -> userDeviceRepository.findByUserId(userId).stream()) // Fetch UserDevice by userId and return as Stream
                                 .map(UserDevice::getNotificationToken) // Get notification tokens
                                 .filter(Objects::nonNull) // Filter out any null tokens
                                 .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }


    public List<String> getAdminEmails() {
        // Fetch the admin role from RoleRepository
        Optional<Role> adminRoleOpt = roleRepository.findByRole(RoleName.ROLE_ADMIN);

        // Ensure the role exists before proceeding
        if (adminRoleOpt.isPresent()) {
            Role adminRole = adminRoleOpt.get();

            // Fetch users with the admin role from UserRepository
            return userRepository.findByRolesContaining(adminRole)
                                 .stream()
                                 .map(User::getEmail)  // Assuming User entity has an email field
                                 .collect(Collectors.toList());
        }

        // Return an empty list if ROLE_ADMIN does not exist
        return Collections.emptyList();
    }


}