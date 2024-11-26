package com.ecoprint.printmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.model.DeviceType;
import com.ecoprint.printmanagement.model.UserDevice;
import com.ecoprint.printmanagement.service.UserDeviceService;


@RestController
@RequestMapping("/api/user-devices")
public class UserDeviceController {

	@Autowired
    private UserDeviceService userDeviceService;

    @PostMapping("/register")
    public ResponseEntity<UserDevice> registerDevice(
            @RequestParam Long userId,
            @RequestParam String deviceId,
            @RequestParam DeviceType deviceType,
            @RequestParam(required = false) String notificationToken) {
    	UserDevice device = userDeviceService.registerOrRetrieveDevice(userId, deviceId, deviceType, notificationToken);

        return ResponseEntity.ok(device);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutDevice(@RequestParam String deviceId) {
        userDeviceService.logoutDevice(deviceId);
        return ResponseEntity.ok("Device logged out successfully.");
    }

    @GetMapping("/{userId}/devices")
    public ResponseEntity<List<UserDevice>> getUserDevices(@PathVariable Long userId) {
        List<UserDevice> devices = userDeviceService.listUserDevices(userId);
        return ResponseEntity.ok(devices);
    }

}
