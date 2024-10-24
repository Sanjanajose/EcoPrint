package com.ecoprint.printmanagement.service; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.exception.ResourceAlreadyInUseException;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException; // Make sure to import this exception
import com.ecoprint.printmanagement.model.ActivityLog;
import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.payload.RegistrationRequest;
import com.ecoprint.printmanagement.repository.ActivityLogRepository;
import com.ecoprint.printmanagement.repository.UserRepository;
import com.ecoprint.printmanagement.service.RoleService;

import java.util.*;
import java.util.stream.Collectors;
import com.ecoprint.printmanagement.model.payload.RoleAssignmentRequest;


@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;
    
    @Autowired
    private ActivityLogRepository activityLogRepository; 

    // Retrieve a paginated list of all users.
    public Page<User> getAllUsers(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return userRepository.findAll(pageable);
    }
    
 // Create a new user based on the provided registration request.
    public User createUser(RegistrationRequest request, Set<String> roleNames) {
        // Check if the email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyInUseException("Email", "Address", request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Convert Set<String> roleNames to Set<RoleName>
        Set<RoleName> roleEnums = roleNames.stream()
                .map(RoleName::valueOf) // Convert each string to RoleName enum
                .collect(Collectors.toSet());

        // Assign roles provided by the admin
        Set<Role> roles = getRolesForUser(roleEnums);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private Set<Role> getRolesForUser(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(roleService::getRoleByName) // Assuming this returns Role
                .collect(Collectors.toSet());
    }


    
    public User assignRoles(Long userId, RoleAssignmentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Convert role names from strings to RoleName enums
        Set<RoleName> roleNames = request.getRoleNames().stream()
                .map(RoleName::valueOf) // Convert String to RoleName enum
                .collect(Collectors.toSet());

        // Get the corresponding Role entities
        Set<Role> roles = roleService.getRolesByName(roleNames);

        // Assign the roles to the user
        user.setRoles(roles);
        return userRepository.save(user);
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        userRepository.delete(user);
    }
    
    public List<ActivityLog> getActivityLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityLogRepository.findAll(pageable).getContent();
    }
}
