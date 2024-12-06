package com.ecoprint.printmanagement.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.springframework.stereotype.Service;

@Service

public class RawPrinterService {

	
	 public void sendPrintJob(String printerIp, int port, InputStream fileData, String fileName, int trayNumber) throws IOException {
	        try (Socket socket = new Socket(printerIp, port);
	             OutputStream out = socket.getOutputStream()) {

	            // PCL command for tray selection
	            String trayCommand = String.format("\u001B&l%dH", trayNumber); // Esc & l [trayNumber] H
	            out.write(trayCommand.getBytes());

	            // Send file data
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            while ((bytesRead = fileData.read(buffer)) != -1) {
	                out.write(buffer, 0, bytesRead);
	            }
	            out.flush();

	            System.out.println("Print job sent successfully to " + printerIp + ":" + port);
	        } catch (Exception e) {
	            throw new IOException("Failed to send print job: " + e.getMessage(), e);
	        }
	    }
}
