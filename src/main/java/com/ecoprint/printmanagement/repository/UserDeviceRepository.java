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

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.UserDevice;
import com.ecoprint.printmanagement.model.token.RefreshToken;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    @Override
    Optional<UserDevice> findById(Long id);

    Optional<UserDevice> findByRefreshToken(RefreshToken refreshToken);

    Optional<UserDevice> findByUserIdAndDeviceId(Long userId, String userDeviceId);
    
    
    
    List<UserDevice> findByUserId(Long userId);


       
        
        
            Optional<UserDevice> findByDeviceId(String deviceId);
            
            
            void deleteByDeviceId(String deviceId);
        
            List<UserDevice> findAllByUserId(Long userId);
            
            
            @Query("SELECT d.notificationToken FROM UserDevice d WHERE d.user.id = :userId AND d.isRefreshActive = true")
            List<String> findActiveNotificationTokensByUserId(@Param("userId") Long userId);


            @Query("SELECT ud.notificationToken FROM UserDevice ud WHERE ud.user.id = :userId")
            Optional<String> findDeviceTokenByUserId(@Param("userId") long userId);
}
