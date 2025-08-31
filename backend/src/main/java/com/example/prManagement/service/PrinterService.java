package com.example.prManagement.service;

import com.example.prManagement.model.Printer;
import com.example.prManagement.repository.PrinterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    @Autowired
    private EmailService emailService;


    private volatile int totalIpsToScan = 0;
    private AtomicInteger ipsScannedCount = new AtomicInteger(0);

    private volatile boolean discoveryInProgress = false;
    private final AtomicInteger newPrintersFound = new AtomicInteger(0);
    private volatile boolean refreshInProgress = false;

    public List<Printer> getAllAndUpdatePrinters() {
        System.out.println("Starting update for existing printers with latest SNMP data...");
        refreshInProgress = true;
        List<Printer> printers = printerRepository.findAll();

        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (Printer printer : printers) {
            executor.submit(() -> {
                try {
                    boolean reachable = false;

                    String name = snmpService.getPrinterName(printer.getIpAddress());
                    String serialNumber = snmpService.getSerialNumber(printer.getIpAddress());
                    String manufacturer= snmpService.getManufacturer(printer.getIpAddress());
                    String model= snmpService.getModel(printer.getIpAddress());
                    String status = snmpService.getStatus(printer.getIpAddress());

                    if (name != null) {
                        printer.setName(name);
                        reachable = true;
                    }
                    if (serialNumber != null) {
                        printer.setSerialNumber(serialNumber);
                        reachable = true;
                    }
                    if (manufacturer != null) {
                        printer.setManufacturer(manufacturer);
                        reachable = true;
                    }
                    if (model != null) {
                        printer.setModel(model);
                        reachable = true;
                    }
                    if (status != null) {
                        printer.setStatus(status);
                    }

                    Integer tonerLevel = snmpService.getTonerLevel(printer.getIpAddress(), printer.getModel());
                    if (tonerLevel != null) {
                        printer.setTonerLevel(tonerLevel);
                        reachable = true;
                    }
                    Integer pageCount = snmpService.getPageCount(printer.getIpAddress(), printer.getModel());
                    if (pageCount != null) {
                        printer.setPageCount(pageCount);
                        reachable = true;
                    }

                    if (reachable) {
                        printer.setSnmpReachable(true);
                        printer.setLastRefreshTime(LocalDateTime.now());
                        System.out.println("Refreshed printer: " + printer.getIpAddress() + ", name: " + name
                        +", snumb:"+serialNumber+", manufacturer: "+ manufacturer+", model"+model+", status"+ status+", tonerLevel"+tonerLevel
                        +", pageCount: "+pageCount);
                    } else {
                        printer.setSnmpReachable(false);
                        printer.setStatus("Unreachable");
                        System.out.println("Printer unreachable: " + printer.getIpAddress());
                    }

                    printerRepository.save(printer);
                } catch (Exception e) {
                    printer.setSnmpReachable(false);
                    printer.setStatus("Unreachable");
                    printerRepository.save(printer);
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
                boolean reachable = false;

                String name = snmpService.getPrinterName(printer.getIpAddress());
                String serialNumber = snmpService.getSerialNumber(printer.getIpAddress());
                String manufacturer= snmpService.getManufacturer(printer.getIpAddress());
                String model= snmpService.getModel(printer.getIpAddress());
                String status = snmpService.getStatus(printer.getIpAddress());

                if (name != null) {
                        printer.setName(name);
                        reachable = true;
                    }
                    if (serialNumber != null) {
                        printer.setSerialNumber(serialNumber);
                        reachable = true;
                    }
                    if (manufacturer != null) {
                        printer.setManufacturer(manufacturer);
                        reachable = true;
                    }
                    if (model != null) {
                        printer.setModel(model);
                        reachable = true;
                    }
                    if (status != null) {
                        printer.setStatus(status);
                    }

                    Integer tonerLevel = snmpService.getTonerLevel(printer.getIpAddress(), printer.getModel());
                    if (tonerLevel != null) {
                        printer.setTonerLevel(tonerLevel);
                        reachable = true;
                    }
                    Integer pageCount = snmpService.getPageCount(printer.getIpAddress(), printer.getModel());
                    if (pageCount != null) {
                        printer.setPageCount(pageCount);
                        reachable = true;
                    }

                    if (reachable) {
                        printer.setSnmpReachable(true);
                        printer.setLastRefreshTime(LocalDateTime.now());
                        System.out.println("Refreshed printer: " + printer.getIpAddress() + ", name: " + name
                        +", snumb:"+serialNumber+", manufacturer: "+ manufacturer+", model"+model+", status"+ status+", tonerLevel"+tonerLevel
                        +", pageCount: "+pageCount);
                    } else {
                        printer.setSnmpReachable(false);
                        printer.setStatus("Unreachable");
                        System.out.println("Printer unreachable: " + printer.getIpAddress());
                    }

                    printerRepository.save(printer);
                } catch (Exception e) {
                    printer.setSnmpReachable(false);
                    printer.setStatus("Unreachable");
                    printerRepository.save(printer);
                    System.err.println("Error refreshing printer " + printer.getIpAddress() + ": " + e.getMessage());
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
        newPrintersFound.set(0);
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
                                newPrinter.setStatus(snmpService.getStatus(ipAddress));
                                newPrinter.setLastRefreshTime(LocalDateTime.now());
                                printerRepository.save(newPrinter);
                                newPrintersFound.incrementAndGet();
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
                                printer.setStatus(snmpService.getStatus(ipAddress));
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
    public int getNumberOfNewPrinters() {
        return newPrintersFound.get();
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


    public byte[] exportPrintersToExcel(List<Printer> printersToExport) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Printers");


            Row headerRow = sheet.createRow(0);

            String[] headers = {"ID", "IP Address", "Name", "Toner Level", "Page Count", "Serial Number", "Manufacturer", "Model", "Status", "Last Refresh Time"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }


            int rowNum = 1;
            for (Printer printer : printersToExport) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(printer.getId() != null ? printer.getId() : -1);
                row.createCell(1).setCellValue(printer.getIpAddress());
                row.createCell(2).setCellValue(printer.getName() != null ? printer.getName() : "N/A");

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
                row.createCell(8).setCellValue(printer.getStatus() != null ? printer.getStatus() : "N/A");
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

    public byte[] exportPrintersToExcel() throws IOException {
        List<Printer> allPrinters = printerRepository.findAll();
        return exportPrintersToExcel(allPrinters);
    }

    public byte[] exportPrintersByIpAddressesToExcel(List<String> ipAddresses) throws IOException {
        List<Printer> printersToExport = new ArrayList<>();

        for (String ipAddress : ipAddresses) {
            Optional<Printer> existingPrinter = printerRepository.findByIpAddress(ipAddress);
            if (existingPrinter.isPresent()) {
                printersToExport.add(existingPrinter.get());
            } else {
                Printer newPrinter = new Printer();
                newPrinter.setIpAddress(ipAddress);
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
                                matchesFilter = printer.getTonerLevel() != null && printer.getTonerLevel() != -2 && printer.getTonerLevel() < 20;
                                break;
                            case "tonerMedium":
                                matchesFilter = printer.getTonerLevel() != null && printer.getTonerLevel() >= 20 && printer.getTonerLevel() < 50;
                                break;
                            case "tonerHigh":
                                matchesFilter = printer.getTonerLevel() != null && printer.getTonerLevel() >= 50;
                                break;
                            case "tonerUnknown":
                                matchesFilter = printer.getTonerLevel() == null || printer.getTonerLevel() == -2;
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
                            case "printing":
                                matchesFilter = printer.getStatus() != null && Objects.equals(printer.getStatus(), "Printing");
                                break;
                            case "online":
                                matchesFilter = printer.getStatus() != null && Objects.equals(printer.getStatus(), "Online");
                                break;
                            case "unreachable":
                                matchesFilter = !printer.isSnmpReachable();
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

        // status
        long printingPrinters = allPrinters.stream()
                .filter(printer -> "Printing".equalsIgnoreCase(printer.getStatus()))
                .count();

        long idlePrinters = allPrinters.stream()
                .filter(printer -> "Idle".equalsIgnoreCase(printer.getStatus()))
                .count();

        long onlinePrinters = allPrinters.stream()
                .filter(printer -> printer.isSnmpReachable())
                .count();

        long warmingUpPrinters = allPrinters.stream()
                .filter(printer -> "Warming Up".equalsIgnoreCase(printer.getStatus()))
                .count();


        long lowTonerPrinters = allPrinters.stream()
                .filter(printer -> printer.getTonerLevel() != null && printer.getTonerLevel() != -2 && printer.getTonerLevel() < 20)
                .count();

        long highPageCountPrinters = allPrinters.stream()
                .filter(printer -> printer.getPageCount() != null && printer.getPageCount() >= 50000)
                .count();

        long unknownTonerLevelPrinters = allPrinters.stream()
                .filter(printer -> printer.getTonerLevel() == null || printer.getTonerLevel() == -2)
                .count();

        long unreachablePrinters = allPrinters.stream()
                .filter(printer -> !printer.isSnmpReachable())
                .count();

        return Map.of(
                "totalPrinters", totalPrinters,
                "lowTonerPrinters", lowTonerPrinters,
                "highPageCountPrinters", highPageCountPrinters,
                "unknownTonerLevelPrinters", unknownTonerLevelPrinters,
                "printingPrinters", printingPrinters,
                "idlePrinters", idlePrinters,
                "onlinePrinters", onlinePrinters,
                "warmingUpPrinters", warmingUpPrinters,
                "unreachablePrinters", unreachablePrinters
        );
    }
    public void deleteSinglePrinter(Long id) {
        printerRepository.deleteById(id);
    }
    public void refreshPrintersByIpAddresses(List<String> ipAddresses) {
        if (ipAddresses == null || ipAddresses.isEmpty()) {
            System.out.println("No printer IP addresses provided to refresh.");
            return;
        }
        List<Printer> printers = printerRepository.findByIpAddressIn(ipAddresses);

        if (printers.isEmpty()) {
            System.out.println("No printers found with the provided IP addresses.");
            return;
        }

        System.out.println("Starting update for " + printers.size() + " specified printers with latest SNMP data...");
        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (Printer printer : printers) {
            executor.submit(() -> {
                try {
                    boolean reachable = false;

                    String name = snmpService.getPrinterName(printer.getIpAddress());
                    String serialNumber = snmpService.getSerialNumber(printer.getIpAddress());
                    String manufacturer= snmpService.getManufacturer(printer.getIpAddress());
                    String model= snmpService.getModel(printer.getIpAddress());
                    String status = snmpService.getStatus(printer.getIpAddress());

                    if (name != null) {
                        printer.setName(name);
                        reachable = true;
                    }
                    if (serialNumber != null) {
                        printer.setSerialNumber(serialNumber);
                        reachable = true;
                    }
                    if (manufacturer != null) {
                        printer.setManufacturer(manufacturer);
                        reachable = true;
                    }
                    if (model != null) {
                        printer.setModel(model);
                        reachable = true;
                    }
                    if (status != null) {
                        printer.setStatus(status);
                    }

                    Integer tonerLevel = snmpService.getTonerLevel(printer.getIpAddress(), printer.getModel());
                    if (tonerLevel != null) {
                        printer.setTonerLevel(tonerLevel);
                        reachable = true;
                    }
                    Integer pageCount = snmpService.getPageCount(printer.getIpAddress(), printer.getModel());
                    if (pageCount != null) {
                        printer.setPageCount(pageCount);
                        reachable = true;
                    }

                    if (reachable) {
                        printer.setSnmpReachable(true);
                        printer.setLastRefreshTime(LocalDateTime.now());
                        System.out.println("Refreshed printer: " + printer.getIpAddress() + ", name: " + name
                                +", snumb:"+serialNumber+", manufacturer: "+ manufacturer+", model"+model+", status"+ status+", tonerLevel"+tonerLevel
                                +", pageCount: "+pageCount);
                    } else {
                        printer.setSnmpReachable(false);
                        printer.setStatus("Unreachable");
                        System.out.println("Printer unreachable: " + printer.getIpAddress());
                    }

                    printerRepository.save(printer);
                } catch (Exception e) {
                    printer.setSnmpReachable(false);
                    printer.setStatus("Unreachable");
                    printerRepository.save(printer);
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
            System.out.println("Specified printer data refresh complete.");
        }
    }
    
    //@Scheduled(fixedRate = 600000)
    @Scheduled(cron = "0 30 8,17 * * ?")
    public void scheduledPrinterReport() {
        System.out.println("Starting scheduled printer refresh, report generation, and email.");
        getAllAndUpdatePrinters();

        try {
            List<Printer> offlinePrinters = getFilteredPrinters(null, "none", "unreachable",null, null);

            ByteArrayOutputStream excelStream = new ByteArrayOutputStream();
            byte[] excelBytes = exportPrintersToExcel(offlinePrinters);

            LocalDateTime now = LocalDateTime.now();
            String fileName = String.format("printer_report_%s.xlsx", 
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")));
            emailService.sendEmailWithAttachment(
                    "<email of the person who will receive the report>",// !!!Replace with your email address when cloning this repository
                    "Imprimantes offline",
                    "les imprimantes offline.",
                    excelBytes,
                    fileName
            );

        } catch (IOException e) {
            System.err.println("Error generating or sending the Excel report: " + e.getMessage());
        }
    }
}