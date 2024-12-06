package com.ecoprint.printmanagement.integration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecoprint.printmanagement.service.PrinterMonitoringService;

@Component
public class PrinterMonitoringScheduler {

    @Autowired
    private PrinterMonitoringService printerMonitoringService; // Service to monitor printer status

    //@Autowired
    //private NotificationService notificationService; // To send notifications

    @Scheduled(fixedRate = 30000) // Poll every 30 seconds
    public void monitorPrinterErrors() {
        try {
            List<String> printers = getAllPrinters(); // Fetch list of printer IPs
            for (String printerIp : printers) {
                String errorStatus = printerMonitoringService.getPrinterErrorStatus(printerIp);
               /* if (!errorStatus.equalsIgnoreCase("No Error")) {
                    notificationService.notifyError(printerIp, errorStatus); // Trigger notification
                }*/
            }
        } catch (Exception e) {
            System.err.println("Error during printer monitoring: " + e.getMessage());
        }
    }

    private List<String> getAllPrinters() {
        // Fetch printer IPs (from database, properties file, etc.)
        return List.of("10.255.254.101", "10.255.254.102"); // Example IPs
    }
}
