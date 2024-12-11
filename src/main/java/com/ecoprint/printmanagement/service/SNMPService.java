package com.ecoprint.printmanagement.service;



import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class SNMPService {

    private static final String COMMUNITY = "public";
    private static final int SNMP_VERSION = SnmpConstants.version2c;

    public List<String> fetchTrayStatuses(String printerIp, String oid) throws Exception {
        List<String> trayStatuses = new ArrayList<>();

        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(COMMUNITY));
        target.setVersion(SNMP_VERSION);
        target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161"));
        target.setTimeout(3000);
        target.setRetries(1);

        OID oidRequest = new OID(oid);
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oidRequest));
        pdu.setType(PDU.GETNEXT);

        boolean finished = false;
        while (!finished) {
            PDU response = snmp.send(pdu, target).getResponse();
            if (response == null) {
                finished = true;
                break;
            }
            VariableBinding vb = response.get(0);
            if (!vb.getOid().startsWith(oidRequest)) {
                finished = true;
                break;
            }
            trayStatuses.add(vb.getOid() + " = " + vb.getVariable().toString());
            pdu.setRequestID(new Integer32(0));
            pdu.set(0, vb);
        }

        snmp.close();
        return trayStatuses;
    }
}
