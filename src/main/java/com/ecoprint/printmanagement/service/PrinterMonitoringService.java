package com.ecoprint.printmanagement.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;
@Service
public class PrinterMonitoringService {
	private Snmp snmp;
	public PrinterMonitoringService() throws Exception {
    	// Initialize SNMP with UDP transport
    	TransportMapping<?> transport = new DefaultUdpTransportMapping();
    	snmp = new Snmp(transport);
    	transport.listen();
	}
	public String getPrinterStatus(String printerIp) throws Exception {
    	// Create SNMP target
    	CommunityTarget target = new CommunityTarget();
    	target.setCommunity(new OctetString("public")); // Default SNMP community string
    	target.setAddress(new UdpAddress(printerIp + "/161")); // Printer IP and SNMP port
    	target.setRetries(2);
    	target.setTimeout(1500);
    	target.setVersion(SnmpConstants.version2c);
    	// Create PDU for SNMP GET
    	PDU pdu = new PDU();
    	pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.3.5.1.1.1"))); // Printer status OID
    	pdu.setType(PDU.GET);
    	// Send SNMP request
    	ResponseEvent responseEvent = snmp.get(pdu, target);
    	PDU responsePDU = responseEvent.getResponse();
    	if (responsePDU != null) {
        	return responsePDU.get(0).getVariable().toString();
    	} else {
        	throw new Exception("No response from printer");
    	}
	}
	public String getPrinterErrortatus(String printerIp) throws Exception {
    	// Create SNMP target
    	CommunityTarget target = new CommunityTarget();
    	target.setCommunity(new OctetString("public")); // Default SNMP community string
    	target.setAddress(new UdpAddress(printerIp + "/161")); // Printer IP and SNMP port
    	target.setRetries(2);
    	target.setTimeout(1500);
    	target.setVersion(SnmpConstants.version2c);
    	// Create PDU for SNMP GET
    	PDU pdu = new PDU();
    	pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.3.5.1.2.1"))); // Printer Error status OID
    	pdu.setType(PDU.GET);
    	// Send SNMP request
    	ResponseEvent responseEvent = snmp.get(pdu, target);
    	PDU responsePDU = responseEvent.getResponse();
    	if (responsePDU != null) {
        	return responsePDU.get(0).getVariable().toString();
    	} else {
        	throw new Exception("No response from printer");
    	}
	}
    private static final int SNMP_PORT = 161;
	private static final int TIMEOUT = 1500; // 1.5 seconds
	private static final int RETRIES = 2;
public Map<String, Integer> getTonerLevels(String printerIp, String community) throws IOException {
    Map<String, Integer> tonerLevels = new HashMap<>();
    // Replace these OIDs with the correct ones for your printer
    Map<String, String> tonerOids = Map.of(
        "Black", "1.3.6.1.2.1.43.11.1.1.9.1.1",
        "Cyan", "1.3.6.1.2.1.43.11.1.1.9.1.2",
        "Magenta", "1.3.6.1.2.1.43.11.1.1.9.1.3",
        "Yellow", "1.3.6.1.2.1.43.11.1.1.9.1.4"
    );
    for (Map.Entry<String, String> entry : tonerOids.entrySet()) {
        String color = entry.getKey();
        String oid = entry.getValue();
        try {
            // Query the printer for the toner level
            String response = getSnmpData(printerIp, oid, community);
            // Convert response to an integer and add to the map
            tonerLevels.put(color, Integer.parseInt(response));
        } catch (Exception e) {
            // Handle specific errors or log missing data for specific colors
            System.err.println("Error retrieving toner level for " + color + ": " + e.getMessage());
        }
    }
    return tonerLevels;
}
private String getSnmpData(String printerIp, String oid, String community) throws IOException {
    // Create SNMP target
    CommunityTarget target = new CommunityTarget();
    target.setCommunity(new OctetString(community));
    target.setAddress(new UdpAddress(printerIp + "/161"));
    target.setRetries(2);
    target.setTimeout(1500);
    target.setVersion(SnmpConstants.version2c);
    // Create PDU for SNMP GET
    PDU pdu = new PDU();
    pdu.add(new VariableBinding(new OID(oid)));
    pdu.setType(PDU.GET);
    // Send SNMP request
    ResponseEvent responseEvent = snmp.get(pdu, target);
    PDU responsePDU = responseEvent.getResponse();
    if (responsePDU != null) {
        return responsePDU.get(0).getVariable().toString();
    } else {
        throw new IOException("No response from printer for OID: " + oid);
    }
}
	private CommunityTarget createTarget(String ip, String community) {
    	CommunityTarget target = new CommunityTarget();
    	target.setCommunity(new OctetString(community));
    	target.setVersion(SnmpConstants.version2c);
    	target.setAddress(new UdpAddress(ip + "/" + SNMP_PORT));
    	target.setTimeout(TIMEOUT);
    	target.setRetries(RETRIES);
    	return target;
	}
    public Map<Integer, String> getTrayStatus(String printerIp) throws Exception {
        String community = "public"; // Replace with actual SNMP community string
        int snmpPort = 161; // Default SNMP port
        // SNMP target configuration
        Address targetAddress = GenericAddress.parse("udp:" + printerIp + "/" + snmpPort);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new org.snmp4j.smi.OctetString(community));
        target.setAddress(targetAddress);
        target.setVersion(org.snmp4j.mp.SnmpConstants.version2c);
        // OIDs to fetch tray statuses
        String trayStatusOID = "1.3.6.1.2.1.43.8.2.1.10"; // Tray status OID
        String trayDescOID = "1.3.6.1.2.1.43.8.2.1.18";  // Tray description OID
        // Prepare SNMP request
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
        snmp.listen();
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(trayStatusOID))); // Add tray status OID
        pdu.setType(PDU.GETNEXT);
        // Send SNMP request and process response
        Map<Integer, String> trayStatuses = new HashMap<>();
        PDU responsePDU = snmp.send(pdu, target).getResponse();
        if (responsePDU != null) {
            for (VariableBinding vb : responsePDU.getVariableBindings()) {
                int trayNumber = vb.getOid().last();
                String status = vb.getVariable().toString();
                trayStatuses.put(trayNumber, "1".equals(status) ? "Full" : "Empty");
            }
        } else {
            throw new Exception("No response from printer.");
        }
        snmp.close();
        return trayStatuses;
    }
	public void close() throws Exception {
    	snmp.close();
	}
	//latest
	public long startJob(String printerIp, int printerPort, InputStream fileStream, String fileName, int trayNumber) throws Exception {
	    // Include the tray number in the print job logic
	    System.out.println("Printing on Tray: " + trayNumber);
	    // Example: Add tray-specific commands for the printer
	    String trayCommand = "SET TRAY " + trayNumber;
	    // Send trayCommand to the printer (example depends on the printer protocol)
	    // Rest of the print logic
	    // ...
	    return System.currentTimeMillis(); // Example Job ID
	}
	
	
	//3. Error Handling for Failed Jobs: (6)

	public String getPrinterErrorStatus(String printerIp) throws Exception {
	    // Example OIDs for error statuses (replace with printer-specific OIDs)
	    String errorStatusOid = "1.3.6.1.2.1.43.18.1.1.4.1";
	    
	    // Set up SNMP
	    TransportMapping transport = new DefaultUdpTransportMapping();
	    Snmp snmp = new Snmp(transport);
	    transport.listen();

	    CommunityTarget target = new CommunityTarget();
	    target.setCommunity(new OctetString("public")); // Replace with your community string
	    target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161"));
	    target.setRetries(2);
	    target.setTimeout(1500);
	    target.setVersion(SnmpConstants.version2c);

	    // Fetch error status
	    PDU pdu = new PDU();
	    pdu.add(new VariableBinding(new OID(errorStatusOid)));
	    pdu.setType(PDU.GET);

	    ResponseEvent response = snmp.send(pdu, target);
	    if (response.getResponse() == null) {
	        throw new Exception("SNMP request timed out.");
	    }

	    return response.getResponse().get(0).getVariable().toString();
	}

	
}