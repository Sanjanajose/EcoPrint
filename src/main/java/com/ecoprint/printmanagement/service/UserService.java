<<<<<<< HEAD
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.annotation.CurrentUser;
=======
package com.ecoprint.printmanagement.service;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.annotation.CurrentUser;
import com.ecoprint.printmanagement.exception.FileUploadException;
import com.ecoprint.printmanagement.exception.ResourceAlreadyInUseException;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
>>>>>>> 982c1c6 (Initial commit)
import com.ecoprint.printmanagement.exception.SetAdminAccessException;
import com.ecoprint.printmanagement.exception.UserLogoutException;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.Role;
<<<<<<< HEAD
=======
import com.ecoprint.printmanagement.model.RoleName;
>>>>>>> 982c1c6 (Initial commit)
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.UserDevice;
import com.ecoprint.printmanagement.model.payload.LogOutRequest;
import com.ecoprint.printmanagement.model.payload.RegistrationRequest;
import com.ecoprint.printmanagement.repository.UserRepository;

<<<<<<< HEAD
@Service
public class UserService {

//    private static final Logger logger = Logger.getLogger(UserService.class);
=======

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import net.coobird.thumbnailator.Thumbnails;




@Service
public class UserService {

>>>>>>> 982c1c6 (Initial commit)
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserDeviceService userDeviceService;
    private final RefreshTokenService refreshTokenService;
<<<<<<< HEAD

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleService roleService, UserDeviceService userDeviceService, RefreshTokenService refreshTokenService) {
=======
    private static final Set<String> ALLOWED_FILE_FORMATS = Set.of("jpg", "jpeg", "png");

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleService roleService, 
                        UserDeviceService userDeviceService, RefreshTokenService refreshTokenService) {
>>>>>>> 982c1c6 (Initial commit)
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.userDeviceService = userDeviceService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
<<<<<<< HEAD
     * Finds a user in the database by username
=======
     * Finds a user by username
>>>>>>> 982c1c6 (Initial commit)
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
<<<<<<< HEAD
     * Finds a user in the database by email
=======
     * Finds a user by email
>>>>>>> 982c1c6 (Initial commit)
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
<<<<<<< HEAD
     * Find a user in db by id.
     */
    public Optional<User> findById(Long Id) {
        return userRepository.findById(Id);
    }

    /**
     * Save the user to the database
=======
     * Finds a user by id
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Saves the user to the database
>>>>>>> 982c1c6 (Initial commit)
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
<<<<<<< HEAD
     * Check is the user exists given the email: naturalId
=======
     * Checks if a user with the given email exists
>>>>>>> 982c1c6 (Initial commit)
     */
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
<<<<<<< HEAD
     * Check is the user exists given the username: naturalId
=======
     * Checks if a user with the given username exists
>>>>>>> 982c1c6 (Initial commit)
     */
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

<<<<<<< HEAD

    /**
     * Creates a new user from the registration request
     */
    public User createUser(RegistrationRequest registerRequest) {
        User newUser = new User();
        Boolean isNewUserAsAdmin = registerRequest.getRegisterAsAdmin();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setUsername(registerRequest.getUsername());
        newUser.addRoles(getRolesForNewUser(isNewUserAsAdmin));
        newUser.setActive(true);
        newUser.setEmailVerified(false);
        return newUser;
    }

    /**
     * Performs a quick check to see what roles the new user could be assigned to.
     *
     * @return list of roles for the new user
     */
    private Set<Role> getRolesForNewUser(Boolean isToBeMadeAdmin) {
        Set<Role> newUserRoles = new HashSet<>(roleService.findAll());
        if (!isToBeMadeAdmin) {
            newUserRoles.removeIf(Role::isAdminRole);
        }
//        logger.info("Setting user roles: " + newUserRoles);
        return newUserRoles;
    }

    /**
     * Log the given user out and delete the refresh token associated with it. If no device
     * id is found matching the database for this user, throw a log out exception.
=======
    public User createUser(RegistrationRequest registerRequest) {
        // Check if the email is unique
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceAlreadyInUseException("Email", "Address", registerRequest.getEmail());
        }

        // Check if the username is unique
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResourceAlreadyInUseException("Username", "Name", registerRequest.getUsername());
        }

        // Validate password strength
        validatePasswordStrength(registerRequest.getPassword());

        // Proceed with creating the user
        User newUser = new User();
        
        // Assuming the RegistrationRequest has a field to determine if it's a new admin
        Boolean isNewUserAsAdmin = registerRequest.getRegisterAsAdmin();

        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setUsername(registerRequest.getUsername());
        
        // Assign roles based on the user type
        newUser.setRoles(getRolesForNewUser(isNewUserAsAdmin)); // This method should handle role assignment

        newUser.setActive(true);
        newUser.setEmailVerified(false);
        newUser.setPhone(registerRequest.getPhone());
        newUser.setAddress(registerRequest.getAddress());
        newUser.setGender(registerRequest.getGender());
        newUser.setCountry(registerRequest.getCountry());
        newUser.setDob(registerRequest.getDob());
        newUser.setProfilePicture(registerRequest.getProfilePicture());

        return userRepository.save(newUser);
    }



    /**
     * Validates the strength of the password
     */
    public void validatePasswordStrength(String password) {
        if (password.length() < 8 || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*()].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters long, contain a number, and a special character.");
        }
    }

    /**
     * Determines which roles to assign to a new user
     */
    private Set<Role> getRolesForNewUser(Boolean isAdmin) {
        Set<Role> roles = new HashSet<>();
        Role defaultRole = roleService.findByName("ROLE_USER")
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
        roles.add(defaultRole);
        
        if (isAdmin) {
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

    /**
     * Assigns a role to a user based on the provided role name.
     */
    public User assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        // Check if the current user has permission to assign this role
        if (!currentUserHasPermissionToAssign(roleName)) {
            throw new AccessDeniedException("You don't have permission to assign this role.");
        }

        // Add the role to the user's existing roles
        user.getRoles().add(role);  // This ensures that roles are maintained correctly
        return userRepository.save(user);
    }

    /**
     * Helper method to check if the current user has permission to assign roles
     */
    private boolean currentUserHasPermissionToAssign(String roleName) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Only admins can assign admin roles
        return currentUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Checks if the current user has permission to assign admin roles.
     */
    private boolean currentUserHasPermissionToAssignAdmin() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return currentUser.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    /**
     * Log the user out and remove the associated refresh token.
>>>>>>> 982c1c6 (Initial commit)
     */
    public void logoutUser(@CurrentUser CustomUserDetails currentUser, LogOutRequest logOutRequest) {
        String deviceId = logOutRequest.getDeviceInfo().getDeviceId();
        UserDevice userDevice = userDeviceService.findDeviceByUserId(currentUser.getId(), deviceId)
<<<<<<< HEAD
                .filter(device -> device.getDeviceId().equals(deviceId))
                .orElseThrow(() -> new UserLogoutException(logOutRequest.getDeviceInfo().getDeviceId(), "Invalid device Id supplied. No matching device found for the given user "));

//        logger.isnfo("Removing refresh token associated with device [" + userDevice + "]");
        refreshTokenService.deleteById(userDevice.getRefreshToken().getId());
    }

	public void setAdminAccess(String email) {
		User currentUser = findByEmail(email)
				.orElseThrow(() -> new SetAdminAccessException(email, "Not matching user found"));
		Set<Role> newUserRoles = new HashSet<Role>(roleService.findAllWithoutSuperAdmin());
		
	}
=======
            .filter(device -> device.getDeviceId().equals(deviceId))
            .orElseThrow(() -> new UserLogoutException(logOutRequest.getDeviceInfo().getDeviceId(), 
                "Invalid device Id supplied. No matching device found for the given user"));

        refreshTokenService.deleteById(userDevice.getRefreshToken().getId());
    }

    /**
     * Sets admin access for the specified user.
     */
    public void setAdminAccess(String email) {
        User currentUser = findByEmail(email)
            .orElseThrow(() -> new SetAdminAccessException(email, "No matching user found"));

        Set<Role> newUserRoles = new HashSet<>(roleService.findAllWithoutSuperAdmin());
        currentUser.setRoles(newUserRoles);
        userRepository.save(currentUser);
    }
    
    

    public String uploadProfilePicture(MultipartFile file, int width, int height) {
        // Define the maximum allowed file size (e.g., 5MB)
        long MAX_FILE_SIZE = 5 * 1024 * 1024;  // 5 MB in bytes
        
        // Define the upload directory
        String uploadDir = "uploads/profile-pictures/";
        
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds the maximum limit of 5MB.");
        }

        // Extract the original file extension
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        
        // Ensure the file format is allowed
        if (!ALLOWED_FILE_FORMATS.contains(fileExtension)) {
            throw new FileUploadException("Unsupported file format: " + fileExtension + ". Only JPG, JPEG, and PNG are allowed.");
        }

        // Generate a unique filename with the correct extension
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Create the directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Define the target file path
            Path filePath = uploadPath.resolve(fileName);

            // Determine output format based on file extension
            String outputFormat;
            if (fileExtension.equals("jpeg") || fileExtension.equals("jpg")) {
                outputFormat = "jpg";  // Handle both jpeg and jpg as jpg
            } else if (fileExtension.equals("png")) {
                outputFormat = "png";  // Handle png format
            } else {
                throw new FileUploadException("Unsupported output format: " + fileExtension);  // Extra safety
            }

            // Resize the image to the desired dimensions and save as the appropriate format
            Thumbnails.of(file.getInputStream())
                      .size(width, height)  // Resize to dynamic width and height
                      .outputFormat(outputFormat)  // Use the correct output format (jpg or png)
                      .toFile(filePath.toFile());  // Save to the desired path

            // Return the relative path of the uploaded image
            return "/uploads/profile-pictures/" + fileName;

        } catch (IOException e) {
            throw new FileUploadException("Could not store the file. Error: " + e.getMessage());
        }
    }


    // Helper method to extract the file extension
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";  // No extension
        }
    }

    
    
    
    /**
     * Check if the given email belongs to the same user (by comparing userId).
     */
    public boolean isSameUser(Long userId, String email) {
        Optional<User> userWithEmail = userRepository.findByEmail(email);
        if (userWithEmail.isPresent()) {
            return userWithEmail.get().getId().equals(userId);
        }
        return false;
    }



>>>>>>> 982c1c6 (Initial commit)
}
