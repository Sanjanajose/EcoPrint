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



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.exception.TokenRefreshException;
import com.ecoprint.printmanagement.model.DeviceType;
import com.ecoprint.printmanagement.model.UserDevice;
import com.ecoprint.printmanagement.model.payload.DeviceInfo;
import com.ecoprint.printmanagement.model.token.RefreshToken;
import com.ecoprint.printmanagement.repository.UserDeviceRepository;
import com.ecoprint.printmanagement.repository.UserRepository;
import java.util.List;



@Service
public class UserDeviceService {

	@Autowired
    private final UserDeviceRepository userDeviceRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public UserDeviceService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    /**
     * Find the user device info by user id
     */
    public Optional<UserDevice> findDeviceByUserId(Long userId, String deviceId) {
        return userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId);
    }

    /**
     * Find the user device info by refresh token
     */
    public Optional<UserDevice> findByRefreshToken(RefreshToken refreshToken) {
        return userDeviceRepository.findByRefreshToken(refreshToken);
    }

    /**
     * Creates a new user device and set the user to the current device
     */
    public UserDevice createUserDevice(DeviceInfo deviceInfo) {
        UserDevice userDevice = new UserDevice();
        userDevice.setDeviceId(deviceInfo.getDeviceId());
        userDevice.setDeviceType(deviceInfo.getDeviceType());
        userDevice.setNotificationToken(deviceInfo.getNotificationToken());
        userDevice.setRefreshActive(true);
        return userDevice;
    }

    /**
     * Check whether the user device corresponding to the token has refresh enabled and
     * throw appropriate errors to the client
     */
    void verifyRefreshAvailability(RefreshToken refreshToken) {
        UserDevice userDevice = findByRefreshToken(refreshToken)
                .orElseThrow(() -> new TokenRefreshException(refreshToken.getToken(), "No device found for the matching token. Please login again"));

        if (!userDevice.getRefreshActive()) {
            throw new TokenRefreshException(refreshToken.getToken(), "Refresh blocked for the device. Please login through a different device");
        }
    }
    
       /**
         * Creates a new user device and sets the user to the current device.
         * If the device already exists, updates its details.
         */
    public UserDevice registerOrUpdateDevice(Long userId, String deviceId, DeviceType deviceType, String notificationToken) {
        // Check if the specific device already exists for the user
        Optional<UserDevice> existingDevice = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId);

        UserDevice userDevice;
        if (existingDevice.isPresent()) {
            // Update the existing device record
            userDevice = existingDevice.get();
            userDevice.setDeviceType(deviceType);
            userDevice.setNotificationToken(notificationToken);
        } else {
            // Create a new device entry
            userDevice = new UserDevice();
            userDevice.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)));
            userDevice.setDeviceId(deviceId);
            userDevice.setDeviceType(deviceType);
            userDevice.setNotificationToken(notificationToken);
            userDevice.setRefreshActive(true);
        }

        return userDeviceRepository.save(userDevice);
    }



        /**
         * Check whether the user device corresponding to the token has refresh enabled and
         * throw appropriate errors to the client.
         */
        
        /**
         * List all devices for a user.
         */
        public List<UserDevice> listUserDevices(Long userId) {
            return userDeviceRepository.findAllByUserId(userId);
        }

        /**
         * Logs out a specific device by marking its refresh as inactive.
         */
        public void logoutDevice(String deviceId) {
            UserDevice device = userDeviceRepository.findByDeviceId(deviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + deviceId));
            device.setRefreshActive(false);
            userDeviceRepository.save(device);
        
        

    
}
    
}
