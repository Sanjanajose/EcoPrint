package com.ecoprint.printmanagement.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetails extends User implements UserDetails {

    public CustomUserDetails(final User user) {
        super(user);
    }

    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Fetch both roles and permissions as authorities
        Set<GrantedAuthority> authorities = getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole().name()))
                .collect(Collectors.toSet());

        authorities.addAll(getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .collect(Collectors.toSet()));

        return authorities;
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public String getUsername() {
        return super.getEmail(); // Using email as the username for login purposes
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Modify as required
    }

    @Override
    public boolean isAccountNonLocked() {
        return super.isActive(); // Assuming 'active' means the account is not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Modify as required
    }

    @Override
    public boolean isEnabled() {
        return super.getEmailVerified(); // Assuming email verification means the account is enabled
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId()); // Use ID for hashing
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CustomUserDetails that = (CustomUserDetails) obj;
        return Objects.equals(getId(), that.getId()); // Compare IDs for equality
    }
}
