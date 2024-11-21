package com.ecoprint.printmanagement.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Priority {
    HIGH,
    MEDIUM,
    LOW;

    @JsonCreator
    public static Priority fromString(String value) {
    	 System.out.println("Deserializing value: " + value);
        if (value != null) {
            try {
                return Priority.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null; // or throw a custom exception if needed
            }
        }
        return null; // default case for null or invalid values
    }
}
