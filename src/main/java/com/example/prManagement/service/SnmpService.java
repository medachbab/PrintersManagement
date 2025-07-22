// src/main/java/com/example/prManagement/service/SnmpService.java
package com.example.prManagement.service;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

@Service
public class SnmpService {

    private static final String COMMUNITY = "public";
    private static final int SNMP_VERSION = SnmpConstants.version2c;

    private static final String NAME_OID = "1.3.6.1.2.1.25.3.2.1.3.1";
    private static final String TONER_OID = "1.3.6.1.2.1.43.8.2.1.10.1.1";
    private static final String PAGE_COUNT_OID = "1.3.6.1.2.1.43.10.2.1.4.1.1";

    public String getAsString(String ip, String oid) {
        Snmp snmp = null;
        TransportMapping<UdpAddress> transport = null;
        try {
            Address targetAddress = GenericAddress.parse("udp:" + ip + "/161");
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(COMMUNITY));
            target.setVersion(SNMP_VERSION);
            target.setAddress(targetAddress);
            target.setRetries(1);
            target.setTimeout(3000); // Increased timeout for better reliability

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            transport = new DefaultUdpTransportMapping();
            transport.listen();
            snmp = new Snmp(transport);

            System.out.println("Sending SNMP GET request to " + ip + " for OID " + oid);
            ResponseEvent response = snmp.get(pdu, target);

            if (response.getResponse() == null) {
                System.out.println("No response received for " + oid + " from " + ip);
                return null;
            }

            String value = response.getResponse().get(0).getVariable().toString();
            System.out.println("Received SNMP response: " + value + " for OID " + oid + " from " + ip);
            return value;
        } catch (Exception e) {
            System.err.println("SNMP GET request failed for IP: " + ip + ", OID: " + oid + ". Exception: " + e.getMessage());
            //e.printStackTrace(); // For detailed debugging, uncomment this line
            return null;
        } finally {
            try {
                if (snmp != null) {
                    snmp.close();
                }
                if (transport != null) {
                    transport.close();
                }
            } catch (Exception e) {
                System.err.println("Error closing SNMP resources: " + e.getMessage());
            }
        }
    }

    public String getPrinterName(String ip) {
        return getAsString(ip, NAME_OID);
    }

    public Integer getTonerLevel(String ip) {
        String result = getAsString(ip, TONER_OID);
        try {
            return result != null ? Integer.parseInt(result) : null;
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse toner level '" + result + "' to integer for IP: " + ip + ". Exception: " + e.getMessage());
            return null;
        }
    }

    public Integer getPageCount(String ip) {
        String result = getAsString(ip, PAGE_COUNT_OID);
        try {
            return result != null ? Integer.parseInt(result) : null;
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse page count '" + result + "' to integer for IP: " + ip + ". Exception: " + e.getMessage());
            return null;
        }
    }
}