package com.example.prManagement.service;

import com.example.prManagement.model.Printer;
import com.example.prManagement.repository.PrinterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private volatile boolean refreshInProgress = false;

    public List<Printer> getAllAndUpdatePrinters() {
        System.out.println("Starting update for existing printers with latest SNMP data...");
        refreshInProgress = true;
        List<Printer> printers = printerRepository.findAll();

        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (Printer printer : printers) {
            executor.submit(() -> {
                updatePrinterSnmpData(printer);
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("Printer refresh interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            refreshInProgress = false;
            System.out.println("All existing printers update attempt complete.");
        }
        return printers;
    }

    @Async
    public void refreshSinglePrinter(Long printerId) {
        System.out.println("Starting refresh for printer ID: " + printerId);
        Optional<Printer> optionalPrinter = printerRepository.findById(printerId);
        if (optionalPrinter.isPresent()) {
            Printer printer = optionalPrinter.get();
            updatePrinterSnmpData(printer);
            System.out.println("Finished refresh for printer ID: " + printerId);
        } else {
            System.err.println("Printer with ID " + printerId + " not found for refresh.");
        }
    }

    private void updatePrinterSnmpData(Printer printer) {
        try {
            InetAddress inetAddress = InetAddress.getByName(printer.getIpAddress());
            if (inetAddress.isReachable(1000)) {
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

                if (printer.getLastRefreshTime() == null || !LocalDateTime.now().equals(printer.getLastRefreshTime()) || updated) {
                    printer.setLastRefreshTime(LocalDateTime.now());
                    updated = true;
                }

                if (updated) {
                    printerRepository.save(printer);
                    System.out.println("Updated printer: " + printer.getName() + " at " + printer.getIpAddress() + " (Last Refreshed: " + printer.getLastRefreshTime() + ")");
                } else {
                    System.out.println("Printer: " + printer.getIpAddress() + " data and last refresh time unchanged.");
                }
            } else {
                System.out.println("Printer at " + printer.getIpAddress() + " is not reachable for update. Skipping SNMP data refresh.");
            }
        } catch (IOException e) {
            System.err.println("Network error while updating printer " + printer.getIpAddress() + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("SNMP error while updating printer " + printer.getIpAddress() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Printer> getFilteredPrinters(String searchTerm, String searchType, String filterType, Integer minToner, Integer minPages) {
        System.out.println("Applying filters at DB level. Search Term: '" + searchTerm + "' (Type: " + searchType + "), Filter Type: " + filterType + ", Min Toner: " + minToner + ", Min Pages: " + minPages);

        boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();

        boolean applyMinToner = false;
        boolean applyMinPages = false;

        if ("toner".equals(filterType) && minToner != null) {
            applyMinToner = true;
        } else if ("pages".equals(filterType) && minPages != null) {
            applyMinPages = true;
        } else if ("both".equals(filterType) && minToner != null && minPages != null) {
            applyMinToner = true;
            applyMinPages = true;
        }


        String nameSearch = null;
        String ipAddressSearch = null;

        if (hasSearchTerm) {
            if ("name".equals(searchType)) {
                nameSearch = searchTerm.trim();
            } else if ("ipAddress".equals(searchType)) {
                ipAddressSearch = searchTerm.trim();
            }
        }


        if (nameSearch != null && ipAddressSearch != null) {
            if (applyMinToner && applyMinPages) {
                return printerRepository.findByNameContainingIgnoreCaseAndIpAddressContainingAndTonerLevelGreaterThanEqualAndPageCountGreaterThanEqual(nameSearch, ipAddressSearch, minToner, minPages);
            } else if (applyMinToner) {
                return printerRepository.findByNameContainingIgnoreCaseAndIpAddressContainingAndTonerLevelGreaterThanEqual(nameSearch, ipAddressSearch, minToner);
            } else if (applyMinPages) {
                return printerRepository.findByNameContainingIgnoreCaseAndIpAddressContainingAndPageCountGreaterThanEqual(nameSearch, ipAddressSearch, minPages);
            } else {
                return printerRepository.findByNameContainingIgnoreCaseAndIpAddressContaining(nameSearch, ipAddressSearch);
            }
        } else if (nameSearch != null) {
            if (applyMinToner && applyMinPages) {
                return printerRepository.findByNameContainingIgnoreCaseAndTonerLevelGreaterThanEqualAndPageCountGreaterThanEqual(nameSearch, minToner, minPages);
            } else if (applyMinToner) {
                return printerRepository.findByNameContainingIgnoreCaseAndTonerLevelGreaterThanEqual(nameSearch, minToner);
            } else if (applyMinPages) {
                return printerRepository.findByNameContainingIgnoreCaseAndPageCountGreaterThanEqual(nameSearch, minPages);
            } else {
                return printerRepository.findByNameContainingIgnoreCase(nameSearch);
            }
        } else if (ipAddressSearch != null) {
            if (applyMinToner && applyMinPages) {
                return printerRepository.findByIpAddressContainingAndTonerLevelGreaterThanEqualAndPageCountGreaterThanEqual(ipAddressSearch, minToner, minPages);
            } else if (applyMinToner) {
                return printerRepository.findByIpAddressContainingAndTonerLevelGreaterThanEqual(ipAddressSearch, minToner);
            } else if (applyMinPages) {
                return printerRepository.findByIpAddressContainingAndPageCountGreaterThanEqual(ipAddressSearch, minPages);
            } else {
                return printerRepository.findByIpAddressContaining(ipAddressSearch);
            }
        } else { // No name or IP search term, only toner/page count or none (just filter)
            if (applyMinToner && applyMinPages) {
                return printerRepository.findByTonerLevelGreaterThanEqualAndPageCountGreaterThanEqual(minToner, minPages);
            } else if (applyMinToner) {
                return printerRepository.findByTonerLevelGreaterThanEqual(minToner);
            } else if (applyMinPages) {
                return printerRepository.findByPageCountGreaterThanEqual(minPages);
            } else {
                System.out.println("No filters applied. Returning all printers from DB.");
                return printerRepository.findAll();
            }
        }
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
                        if (!existingIps.contains(ipAddress)) {
                            try {
                                String name = snmpService.getPrinterName(ipAddress);
                                if (name != null && !name.isEmpty()) {
                                    System.out.println("Found new printer at " + ipAddress + " (Name: " + name + ")");
                                    Printer newPrinter = new Printer();
                                    newPrinter.setIpAddress(ipAddress);
                                    newPrinter.setName(name);
                                    newPrinter.setTonerLevel(snmpService.getTonerLevel(ipAddress));
                                    newPrinter.setPageCount(snmpService.getPageCount(ipAddress));
                                    newPrinter.setLastRefreshTime(LocalDateTime.now());
                                    printerRepository.save(newPrinter);
                                } else {
                                    System.out.println("IP " + ipAddress + " is reachable, but not responding to printer SNMP OID or returned empty name.");
                                }
                            } catch (Exception snmpEx) {
                                System.err.println("SNMP data retrieval error for new printer at " + ipAddress + ": " + snmpEx.getClass().getSimpleName() + " - " + snmpEx.getMessage());
                                snmpEx.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Ping error for " + ipAddress + ": " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error during discovery for " + ipAddress + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Discovery interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            discoveryInProgress = false;
            ipsScannedCount.set(0);
            totalIpsToScan = 0;
            System.out.println("Network discovery complete.");
        }
    }

    public int getDiscoveryProgressPercentage() {
        if (discoveryInProgress) {
            if (totalIpsToScan == 0) return 0;
            int progress = (int) (((double) ipsScannedCount.get() / totalIpsToScan) * 100);
            return Math.min(progress, 100);
        } else if (refreshInProgress) {
            return 100;
        }
        return 100;
    }

    public boolean isDiscoveryInProgress() {
        return discoveryInProgress || refreshInProgress;
    }
}