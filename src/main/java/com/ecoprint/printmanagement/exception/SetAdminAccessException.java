package com.ecoprint.printmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.EXPECTATION_FAILED)
public class SetAdminAccessException extends RuntimeException {
	
	private final String user;
	private final String message;
	
	public SetAdminAccessException(String user, String message) {
		super(String.format("Couldn't given Admin Access for [%s]: [%s]", user, message));
		this.user = user;
		this.message = message;
	}

}
