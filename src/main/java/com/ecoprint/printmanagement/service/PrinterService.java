package com.ecoprint.printmanagement.service;

import java.io.IOException;
import java.net.InetAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.repository.PrinterRepository;

@Service

public class PrinterService {
	
	
	@Autowired
	PrinterRepository printerRepository;
	
	
	public boolean isPrinterAvailable(long jobId) {
	    try {
	        InetAddress printerAddress = InetAddress.getByName(getPrinterIpForJob(jobId));
	        return printerAddress.isReachable(2000); // Timeout in milliseconds
	    } catch (IOException e) {
	        System.err.println("Error checking printer availability: " + e.getMessage());
	        return false;
	    }
	}

	private String getPrinterIpForJob(long jobId) {
	    // Retrieve printer IP associated with the jobId from the job metadata
		
		//String ip="10.255.254.101";
	  // return printerRepository.getPrinterIp(jobId);
		return "10.255.254.101";
	}


}
