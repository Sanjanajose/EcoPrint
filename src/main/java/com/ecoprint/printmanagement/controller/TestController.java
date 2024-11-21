package com.ecoprint.printmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.messaging.FirebaseMessaging;

@RestController
@RequestMapping("/api/test")
public class TestController {
	
	

	    @Autowired
	    private FirebaseMessaging firebaseMessaging;

	    @GetMapping("/firebase")
	    public String testFirebase() {
	        return "FirebaseMessaging bean is initialized successfully!";
	    }
	


}
