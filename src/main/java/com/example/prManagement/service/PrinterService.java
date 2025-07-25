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

// Apache POI imports for Excel export
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;


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
                    // Refresh printer data using SNMP
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
                String model= snmpService.getModel(printer.getIpAddress());

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
                    if (inetAddress.isReachable(1000)) { // 1-second timeout
                        String name = snmpService.getPrinterName(ipAddress);
                        if (name != null) {
                            Optional<Printer> existingPrinter = printerRepository.findByIpAddress(ipAddress);
                            if (existingPrinter.isEmpty()) {
                                Printer newPrinter = new Printer();
                                newPrinter.setIpAddress(ipAddress);
                                newPrinter.setName(name);
                                // Retrieve model before getting toner level and page count
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
                                // Optionally, update existing printer data here as well
                                Printer printer = existingPrinter.get();
                                printer.setName(name);
                                // Retrieve model before getting toner level and page count
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

    public List<Printer> getFilteredPrinters(String searchTerm, String searchType, String filterType, Integer minToner, Integer minPages) {
        List<Printer> printers = printerRepository.findAll(); // Start with all printers


        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerCaseSearchTerm = searchTerm.toLowerCase();
            if ("name".equals(searchType)) {
                printers = printers.stream()
                        .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(lowerCaseSearchTerm))
                        .collect(Collectors.toList());
            }
            else if ("ipAddress".equals(searchType)) {
                printers = printers.stream()
                        .filter(p -> p.getIpAddress() != null && p.getIpAddress().contains(lowerCaseSearchTerm))
                        .collect(Collectors.toList());
            }
            else if ("manufacturer".equals(searchType)) {
                printers = printers.stream()
                        .filter(p -> p.getManufacturer() != null && p.getManufacturer().toLowerCase().contains(lowerCaseSearchTerm))
                        .collect(Collectors.toList());
            }
            else if ("model".equals(searchType)) {
                printers = printers.stream()
                        .filter(p -> p.getModel() != null && p.getModel().toLowerCase().contains(lowerCaseSearchTerm))
                        .collect(Collectors.toList());
            }
            else if ("serialNumber".equals(searchType)) {
                printers = printers.stream()
                        .filter(p -> p.getSerialNumber() != null && p.getSerialNumber().toLowerCase().contains(lowerCaseSearchTerm))
                        .collect(Collectors.toList());
            }
        }

        if ("lowToner".equals(filterType)) {
            printers = printers.stream()
                    .filter(p -> p.getTonerLevel() != null && p.getTonerLevel() < 20) // Example threshold
                    .collect(Collectors.toList());
        } else if ("highPageCount".equals(filterType)) {
            printers = printers.stream()
                    .filter(p -> p.getPageCount() != null && p.getPageCount() > 10000) // Example threshold
                    .collect(Collectors.toList());
        }

        if (minToner != null) {
            printers = printers.stream()
                    .filter(p -> p.getTonerLevel() != null && p.getTonerLevel() >= minToner)
                    .collect(Collectors.toList());
        }
        if (minPages != null) {
            printers = printers.stream()
                    .filter(p -> p.getPageCount() != null && p.getPageCount() >= minPages)
                    .collect(Collectors.toList());
        }

        return printers;
    }

    public byte[] exportPrintersToExcel() throws IOException {
        List<Printer> printers = printerRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Printers");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "IP Address", "Name", "Toner Level", "Page Count", "Last Refresh Time"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Printer printer : printers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(printer.getId());
                row.createCell(1).setCellValue(printer.getIpAddress());
                row.createCell(2).setCellValue(printer.getName());
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
                if (printer.getLastRefreshTime() != null) {
                    row.createCell(5).setCellValue(printer.getLastRefreshTime().toString());
                } else {
                    row.createCell(5).setCellValue("N/A");
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}