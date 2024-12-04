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
package com.ecoprint.printmanagement.repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    // Find user by phone number
    Optional<User> findByPhone(String phone);

    // Find users by address
    List<User> findByAddress(String address);

    // Find users by gender
    List<User> findByGender(String gender);

    // Find users by country
    List<User> findByCountry(String country);

    // Find users by date of birth
    List<User> findByDob(LocalDate dob);

    // Find users by profile picture URL
    List<User> findByProfilePicture(String profilePicture);

    boolean existsById(Long userId);

    
    
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END " +
 	       "FROM USER u JOIN u.roles r " +
 	       "WHERE u.id = :userId AND r.role = :roleName")
 	Boolean hasRole(@Param("userId") Long userId, @Param("roleName") RoleName roleName);

    List<User> findByRolesContaining(Role role);
    


    @Query("SELECT u FROM USER u LEFT JOIN FETCH u.backupCodes")
    Page<User> findAllWithBackupCodes(Pageable pageable);

    @Query("SELECT u FROM USER u JOIN u.roles r WHERE r.role = :roleName")
    List<User> findAllByRoleName(@Param("roleName") String roleName);

    
    
    @Query("SELECT u FROM USER u JOIN u.roles r WHERE r.role = :roleName")
    List<User> findAllAdmins(@Param("roleName") RoleName roleName);
    
    @Query("SELECT ud.notificationToken FROM UserDevice ud WHERE ud.user.id = :userId AND ud.isRefreshActive = true")
    List<String> findActiveNotificationTokensByUserId(@Param("userId") Long userId);

}
