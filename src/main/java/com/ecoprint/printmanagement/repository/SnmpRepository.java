package com.ecoprint.printmanagement.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class SnmpRepository {
	
    
	public Map<String, Integer> getJobStates(String printerIp) {
	    // Validate the printer IP
	    if (printerIp == null || printerIp.isEmpty()) {
	        throw new IllegalArgumentException("Printer IP cannot be null or empty.");
	    }

	    Map<String, Integer> jobStates = new HashMap<>();
	    String command = "snmpwalk -v2c -c private " + printerIp + " 1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.5.15";

	    try {
	        Process process = Runtime.getRuntime().exec(command);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

	        String line;
	        while ((line = reader.readLine()) != null) {
	            System.out.println("SNMP Response Line: " + line); // Log each line for debugging
	            
	            // Process only lines containing "INTEGER"
	            /*if (line.contains("INTEGER")) {
	                // Extract job ID
	                String jobId = extractJobId(line);
	                if (jobId == null || jobId.trim().isEmpty()) {
	                    System.err.println("Skipping invalid job ID: " + line);
	                    continue; // Skip this entry
	                }

	                try {
	                    // Parse the state
	                    int state = Integer.parseInt(line.split(":")[1].trim());
	                    if (state < 0) { // Example validation for state
	                        System.err.println("Skipping invalid state: " + line);
	                        continue; // Skip this entry
	                    }

	                    // Add valid job ID and state to the map
	                    jobStates.put(jobId, state);
	                } catch (NumberFormatException e) {
	                    System.err.println("Skipping invalid state format: " + line);
	                    continue; // Skip entries with invalid state format
	                }
	            }*/
	            
	            if (line.contains("INTEGER")) {
	                String jobId = extractJobId(line);
	                if (jobId == null || jobId.trim().isEmpty()) {
	                    System.err.println("Skipping invalid job ID: " + line);
	                    continue; // Skip invalid entries
	                }

	                try {
	                    // Extract and parse the state
	                    int state = Integer.parseInt(line.substring(line.indexOf("INTEGER:") + 8).trim());
	                    if (state < 0) { // Example validation for state
	                        System.err.println("Skipping invalid state: " + line);
	                        continue; // Skip invalid entries
	                    }

	                    // Add valid job ID and state to the map
	                    jobStates.put(jobId, state);
	                } catch (NumberFormatException e) {
	                    System.err.println("Skipping invalid state format: " + line);
	                    continue; // Skip entries with invalid state format
	                }
	            }


	        }

	        reader.close();
	    } catch (IOException e) {
	        throw new RuntimeException("Error executing SNMP command", e);
	    }

	    return jobStates;
	}


    public int getSnmpValue(String printerIp, String oid) {
        String command = "snmpget -v2c -c private " + printerIp + " " + oid;

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();

            reader.close();

            if (line != null && line.contains("INTEGER")) {
                return Integer.parseInt(line.split(":")[1].trim());
            }

        } catch (IOException e) {
            throw new RuntimeException("Error executing SNMP command", e);
        }

        return 0;
    }

    private String extractJobId(String line) {
        String[] parts = line.split("\\.");
        if (parts.length > 1) {
            String jobId = parts[parts.length - 2]; // Second last part is the job ID
            System.out.println("Extracted Job ID: " + jobId);
            if (jobId == null || jobId.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid job ID extracted from line: " + line);
            }
            return jobId;
        } else {
            throw new IllegalArgumentException("Unable to extract job ID from line: " + line);
        }
    }

}
