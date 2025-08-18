package com.example.prManagement.service;

import com.example.prManagement.model.Printer;
import com.example.prManagement.repository.PrinterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Collection; // Added import for Collection

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.util.Map;

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
                try {
                    String name = snmpService.getPrinterName(printer.getIpAddress());
                    String serialNumber = snmpService.getSerialNumber(printer.getIpAddress());
                    String manufacturer= snmpService.getManufacturer(printer.getIpAddress());
                    String model= snmpService.getModel(printer.getIpAddress()); // Retrieve model first

                    if (name != null) {
                        printer.setName(name);
                    }
                    if (serialNumber != null) {
                        printer.setSerialNumber(serialNumber);
                    }
                    if (manufacturer != null) {
                        printer.setManufacturer(manufacturer);
                    }
                    if (model != null) {
                        printer.setModel(model);
                    }

                    Integer tonerLevel = snmpService.getTonerLevel(printer.getIpAddress(), printer.getModel());
                    if (tonerLevel != null) {
                        printer.setTonerLevel(tonerLevel);
                    }
                    Integer pageCount = snmpService.getPageCount(printer.getIpAddress(), printer.getModel());
                    if (pageCount != null) {
                        printer.setPageCount(pageCount);
                    }

                    printer.setLastRefreshTime(LocalDateTime.now());
                    printerRepository.save(printer);
                    System.out.println("Refreshed printer: " + printer.getIpAddress());
                } catch (Exception e) {
                    System.err.println("Error refreshing printer " + printer.getIpAddress() + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Printer refresh interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            refreshInProgress = false;
            System.out.println("All existing printer data refresh complete.");
        }
        return printerRepository.findAll();
    }

    public void refreshSinglePrinter(Long id) {
        Optional<Printer> printerOptional = printerRepository.findById(id);
        printerOptional.ifPresent(printer -> {
            try {
                String name = snmpService.getPrinterName(printer.getIpAddress());
                String serialNumber = snmpService.getSerialNumber(printer.getIpAddress());
                String manufacturer= snmpService.getManufacturer(printer.getIpAddress());
                String model= snmpService.getModel(printer.getIpAddress()); // Retrieve model first

                if (name != null) {
                    printer.setName(name);
                }
                if (serialNumber != null) {
                    printer.setSerialNumber(serialNumber);
                }
                if (manufacturer != null) {
                    printer.setManufacturer(manufacturer);
                }
                if (model != null) {
                    printer.setModel(model);
                }

                // Now call getTonerLevel and getPageCount with the retrieved model
                Integer tonerLevel = snmpService.getTonerLevel(printer.getIpAddress(), printer.getModel());
                if (tonerLevel != null) {
                    printer.setTonerLevel(tonerLevel);
                }
                Integer pageCount = snmpService.getPageCount(printer.getIpAddress(), printer.getModel());
                if (pageCount != null) {
                    printer.setPageCount(pageCount);
                }

                printer.setLastRefreshTime(LocalDateTime.now());
                printerRepository.save(printer);
                System.out.println("Refreshed single printer: " + printer.getIpAddress());
            } catch (Exception e) {
                System.err.println("Error refreshing single printer " + printer.getIpAddress() + ": " + e.getMessage());
            }
        });
    }

    @Async
    public void discoverAndAddPrinters(String subnetPrefix, int startIp, int endIp) {
        if (discoveryInProgress) {
            System.out.println("Discovery already in progress. Please wait.");
            return;
        }

        discoveryInProgress = true;
        ipsScannedCount.set(0);
        totalIpsToScan = endIp - startIp + 1;
        System.out.println("Starting network discovery for subnet: " + subnetPrefix);

        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (int i = startIp; i <= endIp; i++) {
            String ipAddress = subnetPrefix + i;
            executor.submit(() -> {
                try {
                    ipsScannedCount.incrementAndGet();
                    InetAddress inetAddress = InetAddress.getByName(ipAddress);
                    if (inetAddress.isReachable(1000)) { // 1-second
                        String name = snmpService.getPrinterName(ipAddress);
                        if (name != null) {
                            Optional<Printer> existingPrinter = printerRepository.findByIpAddress(ipAddress);
                            if (existingPrinter.isEmpty()) {
                                Printer newPrinter = new Printer();
                                newPrinter.setIpAddress(ipAddress);
                                newPrinter.setName(name);
                                String model = snmpService.getModel(ipAddress);
                                newPrinter.setModel(model);

                                newPrinter.setTonerLevel(snmpService.getTonerLevel(ipAddress, model));
                                newPrinter.setPageCount(snmpService.getPageCount(ipAddress, model));
                                newPrinter.setSerialNumber(snmpService.getSerialNumber(ipAddress));
                                newPrinter.setManufacturer(snmpService.getManufacturer(ipAddress));
                                newPrinter.setLastRefreshTime(LocalDateTime.now());
                                printerRepository.save(newPrinter);
                                System.out.println("Discovered and added printer: " + newPrinter.getName() + " at " + newPrinter.getIpAddress());
                            } else {
                                System.out.println("Printer already exists: " + ipAddress);
                                Printer printer = existingPrinter.get();
                                printer.setName(name);
                                String model = snmpService.getModel(ipAddress);
                                printer.setModel(model);

                                printer.setTonerLevel(snmpService.getTonerLevel(ipAddress, model));
                                printer.setPageCount(snmpService.getPageCount(ipAddress, model));
                                printer.setSerialNumber(snmpService.getSerialNumber(ipAddress));
                                printer.setManufacturer(snmpService.getManufacturer(ipAddress));
                                printer.setLastRefreshTime(LocalDateTime.now());
                                printerRepository.save(printer);
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
        return discoveryInProgress;
    }

    public boolean isRefreshInProgress() {
        return refreshInProgress;
    }


    // Refactored method to export a given list of printers to Excel
    public byte[] exportPrintersToExcel(List<Printer> printersToExport) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Printers");

            // Create header row
            Row headerRow = sheet.createRow(0);
            // Added Serial Number, Manufacturer, Model columns
            String[] headers = {"ID", "IP Address", "Name", "Toner Level", "Page Count", "Serial Number", "Manufacturer", "Model", "Last Refresh Time"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (Printer printer : printersToExport) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(printer.getId() != null ? printer.getId() : -1); // Use -1 or another indicator for new/unknown
                row.createCell(1).setCellValue(printer.getIpAddress());
                row.createCell(2).setCellValue(printer.getName() != null ? printer.getName() : "N/A");
                // Handle potential nulls for tonerLevel and pageCount
                if (printer.getTonerLevel() != null) {
                    row.createCell(3).setCellValue(printer.getTonerLevel());
                } else {
                    row.createCell(3).setCellValue("N/A");
                }
                if (printer.getPageCount() != null) {
                    row.createCell(4).setCellValue(printer.getPageCount());
                } else {
                    row.createCell(4).setCellValue("N/A");
                }
                row.createCell(5).setCellValue(printer.getSerialNumber() != null ? printer.getSerialNumber() : "N/A");
                row.createCell(6).setCellValue(printer.getManufacturer() != null ? printer.getManufacturer() : "N/A");
                row.createCell(7).setCellValue(printer.getModel() != null ? printer.getModel() : "N/A");
                if (printer.getLastRefreshTime() != null) {
                    row.createCell(8).setCellValue(printer.getLastRefreshTime().toString());
                } else {
                    row.createCell(8).setCellValue("N/A");
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Existing method, now calls the refactored one to export all printers
    public byte[] exportPrintersToExcel() throws IOException {
        List<Printer> allPrinters = printerRepository.findAll();
        return exportPrintersToExcel(allPrinters);
    }

    // New method to export printers based on a list of IP addresses
    public byte[] exportPrintersByIpAddressesToExcel(List<String> ipAddresses) throws IOException {
        List<Printer> printersToExport = new ArrayList<>();

        for (String ipAddress : ipAddresses) {
            Optional<Printer> existingPrinter = printerRepository.findByIpAddress(ipAddress);
            if (existingPrinter.isPresent()) {
                printersToExport.add(existingPrinter.get());
            } else {
                // If IP doesn't exist, create a placeholder Printer object
                Printer newPrinter = new Printer();
                newPrinter.setIpAddress(ipAddress);
                // Other fields will remain null and be displayed as "N/A" in Excel
                printersToExport.add(newPrinter);
            }
        }
        return exportPrintersToExcel(printersToExport);
    }


    public List<Printer> getFilteredPrinters(String searchTerm, String searchType, String filterType, Integer minToner, Integer minPages) {
        List<Printer> printers = printerRepository.findAll();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerCaseSearchTerm = searchTerm.toLowerCase();
            printers = printers.stream()
                    .filter(printer -> {
                        switch (searchType) {
                            case "name":
                                return printer.getName() != null && printer.getName().toLowerCase().contains(lowerCaseSearchTerm);
                            case "ipAddress":
                                return printer.getIpAddress() != null && printer.getIpAddress().contains(lowerCaseSearchTerm);
                            case "serialNumber":
                                return printer.getSerialNumber() != null && printer.getSerialNumber().toLowerCase().contains(lowerCaseSearchTerm);
                            case "manufacturer":
                                return printer.getManufacturer() != null && printer.getManufacturer().toLowerCase().contains(lowerCaseSearchTerm);
                            case "model":
                                return printer.getModel() != null && printer.getModel().toLowerCase().contains(lowerCaseSearchTerm);
                            default:
                                return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        if (!"none".equals(filterType)) {
            printers = printers.stream()
                    .filter(printer -> {
                        boolean matchesFilter = false;
                        switch (filterType) {
                            case "tonerLow":
                                matchesFilter = printer.getTonerLevel() != null && printer.getTonerLevel() < 20;
                                break;
                            case "tonerMedium":
                                matchesFilter = printer.getTonerLevel() != null && printer.getTonerLevel() >= 20 && printer.getTonerLevel() < 50;
                                break;
                            case "tonerHigh":
                                matchesFilter = printer.getTonerLevel() != null && printer.getTonerLevel() >= 50;
                                break;
                            case "pagesLow":
                                matchesFilter = printer.getPageCount() != null && printer.getPageCount() < 10000;
                                break;
                            case "pagesMedium":
                                matchesFilter = printer.getPageCount() != null && printer.getPageCount() >= 10000 && printer.getPageCount() < 50000;
                                break;
                            case "pagesHigh":
                                matchesFilter = printer.getPageCount() != null && printer.getPageCount() >= 50000;
                                break;
                            case "lastRefresh24h":
                                matchesFilter = printer.getLastRefreshTime() != null && printer.getLastRefreshTime().isAfter(LocalDateTime.now().minusHours(24));
                                break;
                            case "lastRefresh7d":
                                matchesFilter = printer.getLastRefreshTime() != null && printer.getLastRefreshTime().isAfter(LocalDateTime.now().minusDays(7));
                                break;
                            case "lastRefresh30d":
                                matchesFilter = printer.getLastRefreshTime() != null && printer.getLastRefreshTime().isAfter(LocalDateTime.now().minusDays(30));
                                break;
                        }
                        return matchesFilter;
                    })
                    .collect(Collectors.toList());
        }

        if (minToner != null) {
            printers = printers.stream()
                    .filter(printer -> printer.getTonerLevel() != null && printer.getTonerLevel() >= minToner)
                    .collect(Collectors.toList());
        }

        if (minPages != null) {
            printers = printers.stream()
                    .filter(printer -> printer.getPageCount() != null && printer.getPageCount() >= minPages)
                    .collect(Collectors.toList());
        }

        return printers;
    }

    public Map<String, Object> getDashboardStatistics() {
        List<Printer> allPrinters = printerRepository.findAll();
        long totalPrinters = allPrinters.size();

        // Use a stream to count printers with low toner (e.g., < 20%)
        long lowTonerPrinters = allPrinters.stream()
                .filter(printer -> printer.getTonerLevel() != null && printer.getTonerLevel() < 20)
                .count();

        // Use a stream to count printers with a high page count (e.g., >= 50,000)
        long highPageCountPrinters = allPrinters.stream()
                .filter(printer -> printer.getPageCount() != null && printer.getPageCount() >= 50000)
                .count();

        return Map.of(
                "totalPrinters", totalPrinters,
                "lowTonerPrinters", lowTonerPrinters,
                "highPageCountPrinters", highPageCountPrinters
        );
    }
}