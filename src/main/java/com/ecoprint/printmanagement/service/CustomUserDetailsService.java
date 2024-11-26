package com.ecoprint.printmanagement.service;
 
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
 
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
 
@Service
public class CustomUserDetailsService implements UserDetailsService {
 
    private final UserRepository userRepository;
 
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
 
    /*@Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> dbUser = userRepository.findByEmail(email);
 
        User user = dbUser.orElseThrow(() -> 
            new UsernameNotFoundException("Couldn't find a matching user email in the database for " + email));
 
        // Create CustomUserDetails with user
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
 
        // Authorities are handled by CustomUserDetails' getAuthorities method
        return customUserDetails;
    }*/
 
    public UserDetails loadUserById(Long id) {    
        Optional<User> dbUser = userRepository.findById(id);
 
        User user = dbUser.orElseThrow(() -> 
            new UsernameNotFoundException("Couldn't find a matching user id in the database for " + id));
 
        // Create CustomUserDetails with user
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
 
        // Authorities are handled by CustomUserDetails' getAuthorities method
        return customUserDetails;
        
    }
    
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
       // logger.debug("Attempting to authenticate with identifier: {}", identifier);

        User user = userRepository.findByIdentifier(identifier.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

       // log.debug("Authenticated user: {}", user);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                getAuthorities(user));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
    }


}

