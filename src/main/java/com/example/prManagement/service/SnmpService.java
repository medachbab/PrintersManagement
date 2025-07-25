package com.example.prManagement.service;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SnmpService {

    // Printer Name OID:
    private static final OID SYS_NAME_OID = new OID("1.3.6.1.2.1.1.5.0");
    // Alternative description OID:
    private static final OID HR_DEVICE_DESCR_OID = new OID("1.3.6.1.2.1.25.3.2.1.3.1");

    // OID for printer toner level for general printers (percentage because full capacity=100)
    private static final OID GENERIC_TONER_PERCENTAGE_OID = new OID("1.3.6.1.2.1.43.11.1.1.9.1.1");
    // OID for actual toner level for specific printers like Kyocera
    private static final OID KYOCERA_TONER_LEVEL_ACTUAL_OID = new OID("1.3.6.1.2.1.43.11.1.1.9.1.1");
    // OID for full toner capacity for specific printers like Kyocera
    private static final OID KYOCERA_TONER_LEVEL_CAPACITY_OID = new OID("1.3.6.1.2.1.43.11.1.1.8.1.1");


    // Serial Number OID
    private static final OID SERIAL_NUMBER_OID = new OID("1.3.6.1.2.1.43.5.1.1.17.1");

    // Manufacturer OID
    private static final OID MANUFACTURER_OID = new OID("1.3.6.1.2.1.43.5.1.1.16.1");

    // Model OID
    private static final OID MODEL_OID = new OID("1.3.6.1.2.1.25.3.2.1.3.1");



    private String getSnmpValue(String ipAddress, OID oid) {
        Snmp snmp = null;
        TransportMapping transport = null;
        try {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            Address targetAddress = GenericAddress.parse("udp:" + ipAddress + "/161");
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(targetAddress);
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);

            ResponseEvent responseEvent = snmp.send(pdu, target);

            if (responseEvent != null && responseEvent.getResponse() != null) {
                PDU responsePDU = responseEvent.getResponse();
                if (responsePDU.getVariableBindings().size() > 0) {
                    return responsePDU.getVariableBindings().get(0).getVariable().toString();
                }
            }
        } catch (IOException e) {
            System.err.println("SNMP IOException for " + ipAddress + " and OID " + oid + ": " + e.getMessage());

        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException e) {
                    System.err.println("Error closing SNMP session: " + e.getMessage());
                }
            }
            if (transport != null) {
                try {
                    transport.close();
                } catch (IOException e) {
                    System.err.println("Error closing SNMP transport: " + e.getMessage());
                }
            }
        }
        return null;
    }

    public String getPrinterName(String ipAddress) {
        String name = getSnmpValue(ipAddress, SYS_NAME_OID);
        if (name == null || name.isEmpty() || name.equals("null")) {
            name = getSnmpValue(ipAddress, HR_DEVICE_DESCR_OID);
        }
        if (name != null && name.equals("null")) {
            name = null;
        }
        return name;
    }


    public Integer getTonerLevel(String ipAddress, String model) {
        if (model != null && model.toLowerCase().contains("ecosys p3145dn")) {
            // For ECOSYS P3145dn, calculate percentage from actual and capacity
            String actualValueStr = getSnmpValue(ipAddress, KYOCERA_TONER_LEVEL_ACTUAL_OID);
            String capacityValueStr = getSnmpValue(ipAddress, KYOCERA_TONER_LEVEL_CAPACITY_OID);

            if (actualValueStr != null && !actualValueStr.isEmpty() &&
                    capacityValueStr != null && !capacityValueStr.isEmpty()) {
                try {
                    Integer actual = Integer.parseInt(actualValueStr);
                    Integer capacity = Integer.parseInt(capacityValueStr);

                    if (capacity > 0) {
                        return (int) Math.round(((double) actual / capacity) * 100);
                    } else {
                        System.err.println("ERROR: Toner capacity is zero for " + ipAddress + " (Model: " + model + "). Cannot calculate percentage.");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("ERROR: Could not parse toner actual/capacity values for " + ipAddress + " (Model: " + model + "). " + e.getMessage());
                }
            } else {
                System.err.println("No valid actual or capacity toner values received for " + ipAddress + " (Model: " + model + ").");
            }
            return null;
        } else {
            String value = getSnmpValue(ipAddress, GENERIC_TONER_PERCENTAGE_OID);
            if (value != null && !value.isEmpty()) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    System.err.println("ERROR: Could not parse toner level '" + value + "' for " + ipAddress + ". " + e.getMessage());
                }
            }
            System.err.println("No valid toner level received for " + ipAddress + ".");
            return null;
        }
    }

    public Integer getPageCount(String ipAddress, String model) {
        OID pageCountOid = null;

        if (model != null) {
            if (model.toLowerCase().contains("hp laserjet pro m404dn") || model.toLowerCase().contains("hp laserjet pro m501dn")) {
                pageCountOid = new OID("1.3.6.1.2.1.43.10.2.1.4.1.1");
            } else if (model.toLowerCase().contains("ecosys p3145dn")) {
                pageCountOid = new OID("1.3.6.1.4.1.1347.43.10.1.1.12.1.1");
            } else if (model.toLowerCase().contains("hp laserjet m406")) {
                pageCountOid = new OID("1.3.6.1.4.1.11.2.3.9.4.2.1.1.16.4.1.1.2.0");
            }
        }

        if (pageCountOid == null) {
            System.err.println("WARNING: No specific page count OID found for model '" + model + "'. Returning null.");
            return null;
        }

        String value = getSnmpValue(ipAddress, pageCountOid);
        if (value != null && !value.isEmpty()) {
            try {
                Integer count = Integer.parseInt(value);
                System.out.println("Page count successfully parsed for " + ipAddress + " (Model: " + model + ") with OID " + pageCountOid + ": " + count);
                return count;
            } catch (NumberFormatException e) {
                System.err.println("ERROR: Could not parse page count '" + value + "' for " + ipAddress + " from OID " + pageCountOid + ". " + e.getMessage());
            }
        }
        System.err.println("No valid page count received for " + ipAddress + " (Model: " + model + ") from OID " + pageCountOid + " or value was null/empty.");
        return null;
    }

    public String getSerialNumber(String ipAddress) {
        String name = getSnmpValue(ipAddress, SERIAL_NUMBER_OID);
        if (name != null && name.equals("null")) {
            name = null;
        }
        return name;
    }

    public String getManufacturer(String ipAddress) {
        String name = getSnmpValue(ipAddress, MANUFACTURER_OID);
        if (name != null && name.equals("null")) {
            name = null;
        }
        return name;
    }

    public String getModel(String ipAddress) {
        String name = getSnmpValue(ipAddress, MODEL_OID);
        if (name != null && name.equals("null")) {
            name = null;
        }
        return name;
    }
}