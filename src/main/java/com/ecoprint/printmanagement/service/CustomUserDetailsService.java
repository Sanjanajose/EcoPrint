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

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> dbUser = userRepository.findByEmail(email);

        User user = dbUser.orElseThrow(() -> 
            new UsernameNotFoundException("Couldn't find a matching user email in the database for " + email));
        
        return dbUser.map(CustomUserDetails::new)
        		.orElseThrow(()  -> new UsernameNotFoundException("Couldn't find a matching user email in the database for " + email));

        // Convert roles and permissions to Spring Security's SimpleGrantedAuthority
/**        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getRole().name()))
            .collect(Collectors.toSet());

        authorities.addAll(user.getPermissions().stream()
            .map(permission -> new SimpleGrantedAuthority(permission.name()))
            .collect(Collectors.toSet()));

        // Return a new UserDetails object with the user's roles and permissions
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities); **/
    }

    public UserDetails loadUserById(Long id) {
        Optional<User> dbUser = userRepository.findById(id);

        User user = dbUser.orElseThrow(() -> 
            new UsernameNotFoundException("Couldn't find a matching user id in the database for " + id));

        // Convert roles and permissions to Spring Security's SimpleGrantedAuthority
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getRole().name()))
            .collect(Collectors.toSet());

        authorities.addAll(user.getPermissions().stream()
            .map(permission -> new SimpleGrantedAuthority(permission.name()))
            .collect(Collectors.toSet()));

        // Return a new UserDetails object with the user's roles and permissions
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities);
    }
}
