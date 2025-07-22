package com.example.prManagement.service;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SnmpService {

    // Name OID:
    private static final OID SYS_NAME_OID = new OID("1.3.6.1.2.1.1.5.0");
    // description OID
    private static final OID HR_DEVICE_DESCR_OID = new OID("1.3.6.1.2.1.25.3.2.1.3.1");

    // prtMarkerLifeCount:
    private static final OID PRT_MARKER_LIFE_COUNT_OID = new OID("1.3.6.1.2.1.43.8.2.1.10.1.1");

    // Page Count OID:
    private static final OID PAGE_COUNT_OID = new OID("1.3.6.1.2.1.43.10.2.1.4.1.1");


    private static final int SNMP_PORT = 161;
    private static final String COMMUNITY_STRING = "public";

    private String getSnmpValue(String ipAddress, OID oid) {
        Snmp snmp = null;
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(COMMUNITY_STRING));
            target.setAddress(GenericAddress.parse("udp:" + ipAddress + "/" + SNMP_PORT));
            target.setRetries(1); // Number of retries
            target.setTimeout(1000); // Timeout in milliseconds
            target.setVersion(SnmpConstants.version2c); // Use SNMP v2c

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);

            ResponseEvent responseEvent = snmp.send(pdu, target);
            if (responseEvent != null) {
                PDU responsePDU = responseEvent.getResponse();
                if (responsePDU != null) {
                    if (responsePDU.getErrorStatus() == PDU.noError) {
                        VariableBinding vb = responsePDU.getVariableBindings().firstElement();
                        if (vb != null && vb.getOid().equals(oid)) {
                            // debug infos
                            System.out.println("SNMP Debug: Retrieved OID " + oid + " for " + ipAddress + ". Value: " + vb.getVariable().toString());
                            return vb.getVariable().toString();
                        } else if (vb != null) {
                            System.err.println("SNMP: OID mismatch or not found for " + ipAddress + ". Requested " + oid + ", got " + vb.getOid() + " with value " + vb.getVariable());
                        }
                    } else {
                        System.err.println("SNMP Error for " + ipAddress + " OID " + oid + ": " + responsePDU.getErrorStatusText());
                    }
                } else {
                    System.err.println("SNMP No response PDU for " + ipAddress + " OID " + oid);
                }
            } else {
                System.err.println("SNMP Request timed out for " + ipAddress + " OID " + oid);
            }
        } catch (IOException e) {
            System.err.println("SNMP IOException for " + ipAddress + " OID " + oid + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("SNMP Unexpected error for " + ipAddress + " OID " + oid + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException e) {
                    System.err.println("Error closing SNMP session: " + e.getMessage());
                }
            }
        }
        return null;
    }

    public String getPrinterName(String ipAddress) {
        // Try sysName first, then hrDeviceDescr as a fallback
        String name = getSnmpValue(ipAddress, SYS_NAME_OID);
        if (name == null || name.isEmpty() || name.equals("null")) {
            name = getSnmpValue(ipAddress, HR_DEVICE_DESCR_OID);
        }
        if (name != null && name.equals("null")) { // Handle "null" as string from some agents
            name = null;
        }
        return name;
    }

    public Integer getTonerLevel(String ipAddress) {
        String value = getSnmpValue(ipAddress, PRT_MARKER_LIFE_COUNT_OID);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Could not parse toner level '" + value + "' for " + ipAddress + ". " + e.getMessage());
            }
        }
        return null;
    }

    public Integer getPageCount(String ipAddress) {
        String value = getSnmpValue(ipAddress, PAGE_COUNT_OID);
        if (value != null && !value.isEmpty()) {
            try {
                Integer count = Integer.parseInt(value);
                System.out.println("Page count successfully parsed for " + ipAddress + ": " + count);
                return count;
            } catch (NumberFormatException e) {
                System.err.println("ERROR: Could not parse page count '" + value + "' for " + ipAddress + " from OID " + PAGE_COUNT_OID + ". " + e.getMessage());
            }
        }
        System.err.println("No valid page count value retrieved or parsed for " + ipAddress + " from OID " + PAGE_COUNT_OID + ".");
        return null;
    }
}