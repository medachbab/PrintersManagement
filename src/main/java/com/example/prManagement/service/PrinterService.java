package com.example.prManagement.service;

import com.example.prManagement.model.Printer;
import com.example.prManagement.repository.PrinterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class PrinterService {

    @Autowired
    private PrinterRepository printerRepository;

    @Autowired
    private SnmpService snmpService;

    private volatile int totalIpsToScan = 0;
    private AtomicInteger ipsScannedCount = new AtomicInteger(0);
    private volatile boolean discoveryInProgress = false;

    // Removed: private volatile LocalDateTime lastRefreshTime; (Now per printer)


    public List<Printer> getAllAndUpdatePrinters() {
        System.out.println("Starting update for existing printers with latest SNMP data...");
        List<Printer> printers = printerRepository.findAll();
        for (Printer printer : printers) {
            try {
                // Ensure IP is reachable before attempting SNMP
                InetAddress inetAddress = InetAddress.getByName(printer.getIpAddress());
                if (inetAddress.isReachable(1000)) { // 1 second timeout for ping
                    String name = snmpService.getPrinterName(printer.getIpAddress());
                    Integer toner = snmpService.getTonerLevel(printer.getIpAddress());
                    Integer pageCount = snmpService.getPageCount(printer.getIpAddress());

                    boolean updated = false;
                    if (name != null && !name.equals(printer.getName())) {
                        printer.setName(name);
                        updated = true;
                    }
                    if (toner != null && !toner.equals(printer.getTonerLevel())) {
                        printer.setTonerLevel(toner);
                        updated = true;
                    }
                    if (pageCount != null && !pageCount.equals(printer.getPageCount())) {
                        printer.setPageCount(pageCount);
                        updated = true;
                    }

                    if (updated) {
                        printer.setLastRefreshTime(LocalDateTime.now()); // Set refresh time
                        printerRepository.save(printer); // Save if any field was updated
                        System.out.println("Updated printer: " + printer.getName() + " at " + printer.getIpAddress() + " (Last Refreshed: " + printer.getLastRefreshTime() + ")");
                    } else {
                        // If no SNMP data changed but it was reachable, still update refresh time
                        // This makes 'last_refresh_time' always represent last *check* time.
                        printer.setLastRefreshTime(LocalDateTime.now());
                        printerRepository.save(printer);
                        System.out.println("Printer: " + printer.getIpAddress() + " data unchanged, but last refresh time updated.");
                    }
                } else {
                    System.out.println("Printer at " + printer.getIpAddress() + " is not reachable for update.");
                    // Optionally, you might want to update lastRefreshTime to reflect the check was attempted,
                    // or null it out if printer is consistently unreachable to indicate stale data.
                    // For now, we'll leave it as is if unreachable, retaining its last good timestamp.
                }
            } catch (IOException e) {
                System.err.println("Network error while updating printer " + printer.getIpAddress() + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("SNMP error while updating printer " + printer.getIpAddress() + ": " + e.getMessage());
            }
        }
        System.out.println("All existing printers update attempt complete.");
        return printers;
    }

    public List<Printer> getFilteredPrinters(String nameSearch, Integer minToner, Integer minPages) {
        List<Printer> printers = printerRepository.findAll();

        return printers.stream()
                .filter(printer -> {
                    if (nameSearch != null && !nameSearch.trim().isEmpty()) {
                        String printerName = printer.getName();
                        if (printerName == null || !printerName.toLowerCase().contains(nameSearch.toLowerCase())) {
                            return false;
                        }
                    }
                    if (minToner != null) {
                        Integer tonerLevel = printer.getTonerLevel();
                        if (tonerLevel == null || tonerLevel < minToner) {
                            return false;
                        }
                    }
                    if (minPages != null) {
                        Integer pageCount = printer.getPageCount();
                        if (pageCount == null || pageCount < minPages) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Async
    public void discoverAndAddPrinters(String subnetPrefix, int startIp, int endIp) {
        System.out.println("Starting network discovery for subnet: " + subnetPrefix + startIp + "-" + endIp);

        totalIpsToScan = endIp - startIp + 1;
        ipsScannedCount.set(0);
        discoveryInProgress = true;

        int timeout = 1000;

        List<String> existingIps = printerRepository.findAll().stream()
                .map(Printer::getIpAddress)
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (int i = startIp; i <= endIp; i++) {
            final String ipAddress = subnetPrefix + i;
            executor.submit(() -> {
                try {
                    ipsScannedCount.incrementAndGet();
                    InetAddress inetAddress = InetAddress.getByName(ipAddress);

                    if (inetAddress.isReachable(timeout)) {
                        System.out.println("IP " + ipAddress + " is reachable.");

                        if (!existingIps.contains(ipAddress)) {
                            String name = snmpService.getPrinterName(ipAddress);
                            if (name != null && !name.isEmpty()) {
                                System.out.println("Found new printer at " + ipAddress + " (Name: " + name + ")");
                                Printer newPrinter = new Printer();
                                newPrinter.setIpAddress(ipAddress);
                                newPrinter.setName(name);
                                newPrinter.setTonerLevel(snmpService.getTonerLevel(ipAddress)); // Get initial toner
                                newPrinter.setPageCount(snmpService.getPageCount(ipAddress));   // Get initial page count
                                newPrinter.setLastRefreshTime(LocalDateTime.now()); // Set refresh time for new printer
                                printerRepository.save(newPrinter);
                            } else {
                                System.out.println("IP " + ipAddress + " is reachable, but not responding to printer SNMP OID.");
                            }
                        }
                    }
                } catch (IOException e) {
                    // System.err.println("Could not ping " + ipAddress + ": " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error during SNMP check for " + ipAddress + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Discovery interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            discoveryInProgress = false;
            System.out.println("Network discovery complete.");
            // No global lastRefreshTime here anymore, it's set per printer
        }
    }

    public int getDiscoveryProgressPercentage() {
        if (!discoveryInProgress || totalIpsToScan == 0) {
            return discoveryInProgress ? 0 : 100;
        }
        return (int) (((double) ipsScannedCount.get() / totalIpsToScan) * 100);
    }

    public boolean isDiscoveryInProgress() {
        return discoveryInProgress;
    }

    // Removed: public LocalDateTime getLastRefreshTime() { ... }
    // This method is no longer needed as time is per printer.
}
/*
package com.example.prManagement.service;

import com.example.prManagement.model.Printer;
import com.example.prManagement.repository.PrinterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class PrinterService {

    @Autowired
    private PrinterRepository printerRepository;

    @Autowired
    private SnmpService snmpService;

    private volatile int totalIpsToScan = 0;
    private AtomicInteger ipsScannedCount = new AtomicInteger(0);
    private volatile boolean discoveryInProgress = false;

    // Removed: private volatile LocalDateTime lastRefreshTime; (Now per printer)


    public List<Printer> getAllAndUpdatePrinters() {
        System.out.println("Starting update for existing printers with latest SNMP data...");
        List<Printer> printers = printerRepository.findAll();
        for (Printer printer : printers) {
            try {
                // Ensure IP is reachable before attempting SNMP
                InetAddress inetAddress = InetAddress.getByName(printer.getIpAddress());
                if (inetAddress.isReachable(1000)) { // 1 second timeout for ping
                    String name = snmpService.getPrinterName(printer.getIpAddress());
                    Integer toner = snmpService.getTonerLevel(printer.getIpAddress());
                    Integer pageCount = snmpService.getPageCount(printer.getIpAddress());

                    boolean updated = false;
                    if (name != null && !name.equals(printer.getName())) {
                        printer.setName(name);
                        updated = true;
                    }
                    if (toner != null && !toner.equals(printer.getTonerLevel())) {
                        printer.setTonerLevel(toner);
                        updated = true;
                    }
                    if (pageCount != null && !pageCount.equals(printer.getPageCount())) {
                        printer.setPageCount(pageCount);
                        updated = true;
                    }

                    if (updated) {
                        printer.setLastRefreshTime(LocalDateTime.now()); // Set refresh time
                        printerRepository.save(printer); // Save if any field was updated
                        System.out.println("Updated printer: " + printer.getName() + " at " + printer.getIpAddress() + " (Last Refreshed: " + printer.getLastRefreshTime() + ")");
                    } else {
                        // If no SNMP data changed but it was reachable, still update refresh time
                        // This makes 'last_refresh_time' always represent last *check* time.
                        printer.setLastRefreshTime(LocalDateTime.now());
                        printerRepository.save(printer);
                        System.out.println("Printer: " + printer.getIpAddress() + " data unchanged, but last refresh time updated.");
                    }
                } else {
                    System.out.println("Printer at " + printer.getIpAddress() + " is not reachable for update.");
                    // Optionally, you might want to update lastRefreshTime to reflect the check was attempted,
                    // or null it out if printer is consistently unreachable to indicate stale data.
                    // For now, we'll leave it as is if unreachable, retaining its last good timestamp.
                }
            } catch (IOException e) {
                System.err.println("Network error while updating printer " + printer.getIpAddress() + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("SNMP error while updating printer " + printer.getIpAddress() + ": " + e.getMessage());
            }
        }
        System.out.println("All existing printers update attempt complete.");
        return printers;
    }

    public List<Printer> getFilteredPrinters(String nameSearch, Integer minToner, Integer minPages) {
        List<Printer> printers = printerRepository.findAll();

        return printers.stream()
                .filter(printer -> {
                    if (nameSearch != null && !nameSearch.trim().isEmpty()) {
                        String printerName = printer.getName();
                        if (printerName == null || !printerName.toLowerCase().contains(nameSearch.toLowerCase())) {
                            return false;
                        }
                    }
                    if (minToner != null) {
                        Integer tonerLevel = printer.getTonerLevel();
                        if (tonerLevel == null || tonerLevel < minToner) {
                            return false;
                        }
                    }
                    if (minPages != null) {
                        Integer pageCount = printer.getPageCount();
                        if (pageCount == null || pageCount < minPages) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Async
    public void discoverAndAddPrinters(String subnetPrefix, int startIp, int endIp) {
        System.out.println("Starting network discovery for subnet: " + subnetPrefix + startIp + "-" + endIp);

        totalIpsToScan = endIp - startIp + 1;
        ipsScannedCount.set(0);
        discoveryInProgress = true;

        int timeout = 1000;

        List<String> existingIps = printerRepository.findAll().stream()
                .map(Printer::getIpAddress)
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (int i = startIp; i <= endIp; i++) {
            final String ipAddress = subnetPrefix + i;
            executor.submit(() -> {
                try {
                    ipsScannedCount.incrementAndGet();
                    InetAddress inetAddress = InetAddress.getByName(ipAddress);

                    if (inetAddress.isReachable(timeout)) {
                        System.out.println("IP " + ipAddress + " is reachable.");

                        if (!existingIps.contains(ipAddress)) {
                            String name = snmpService.getPrinterName(ipAddress);
                            if (name != null && !name.isEmpty()) {
                                System.out.println("Found new printer at " + ipAddress + " (Name: " + name + ")");
                                Printer newPrinter = new Printer();
                                newPrinter.setIpAddress(ipAddress);
                                newPrinter.setName(name);
                                newPrinter.setTonerLevel(snmpService.getTonerLevel(ipAddress)); // Get initial toner
                                newPrinter.setPageCount(snmpService.getPageCount(ipAddress));   // Get initial page count
                                newPrinter.setLastRefreshTime(LocalDateTime.now()); // Set refresh time for new printer
                                printerRepository.save(newPrinter);
                            } else {
                                System.out.println("IP " + ipAddress + " is reachable, but not responding to printer SNMP OID.");
                            }
                        }
                    }
                } catch (IOException e) {
                    // System.err.println("Could not ping " + ipAddress + ": " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error during SNMP check for " + ipAddress + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Discovery interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            discoveryInProgress = false;
            System.out.println("Network discovery complete.");
            // No global lastRefreshTime here anymore, it's set per printer
        }
    }

    public int getDiscoveryProgressPercentage() {
        if (!discoveryInProgress || totalIpsToScan == 0) {
            return discoveryInProgress ? 0 : 100;
        }
        return (int) (((double) ipsScannedCount.get() / totalIpsToScan) * 100);
    }

    public boolean isDiscoveryInProgress() {
        return discoveryInProgress;
    }

    // Removed: public LocalDateTime getLastRefreshTime() { ... }
    // This method is no longer needed as time is per printer.
}*/
