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

<<<<<<< HEAD
=======
import java.time.LocalDate;
import java.util.List;
>>>>>>> 982c1c6 (Initial commit)
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoprint.printmanagement.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);
<<<<<<< HEAD
=======

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

>>>>>>> 982c1c6 (Initial commit)
}
