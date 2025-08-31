package com.example.prManagement.controller;

import com.example.prManagement.model.Printer;
import com.example.prManagement.service.PrinterService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000, http://frontend:80")
public class PrinterController {

    @Autowired
    private PrinterService printerService;

    @GetMapping("/printers")
    public List<Printer> listPrinters(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "name") String searchType,
            @RequestParam(required = false, defaultValue = "none") String filterType,
            @RequestParam(required = false) Integer minToner,
            @RequestParam(required = false) Integer minPages) {

        return printerService.getFilteredPrinters(searchTerm, searchType, filterType, minToner, minPages);
    }

    @PostMapping("/discoverPrinters")
    public void discoverPrinters(@RequestParam String subnetPrefix) {
        int startIp = 1;
        int endIp = 254;
        printerService.discoverAndAddPrinters(subnetPrefix, startIp, endIp);
    }

    @PostMapping("/refreshAllPrinters")
    public void refreshAllPrinters() {
        printerService.getAllAndUpdatePrinters();
    }


    @GetMapping("/printers/download/excel")
    public void downloadPrintersExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=printers_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        byte[] excelBytes = printerService.exportPrintersToExcel();
        response.getOutputStream().write(excelBytes);
    }

    /*@GetMapping("/printers/download/excel/selected")
    public void downloadSelectedPrintersExcel(@RequestParam("ipAddresses") String ipAddressesParam,
                                              HttpServletResponse response) throws IOException {
        List<String> ipAddresses = Arrays.stream(ipAddressesParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=selected_printers_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        byte[] excelBytes = printerService.exportPrintersByIpAddressesToExcel(ipAddresses);
        response.getOutputStream().write(excelBytes);
    }*/
    @PostMapping("/printers/download/excel/selected")
    public void downloadSelectedPrintersExcel(@RequestBody List<String> ipAddresses,
                                              HttpServletResponse response) throws IOException {

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=selected_printers_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        byte[] excelBytes = printerService.exportPrintersByIpAddressesToExcel(ipAddresses);
        response.getOutputStream().write(excelBytes);
    }

    @GetMapping("/discoveryProgress")
    public Map<String, Object> getDiscoveryProgress() {
        int progress = printerService.getDiscoveryProgressPercentage();
        int nprintersFound=printerService.getNumberOfNewPrinters();
        boolean inProgress = printerService.isDiscoveryInProgress();
        return Map.of("progress", progress, "inProgress", inProgress, "nprintersFound", nprintersFound);
    }

    @GetMapping("/refreshProgress")
    public Map<String, Object> getRefreshProgress() {
        boolean inProgress = printerService.isRefreshInProgress();
        return Map.of("inProgress", inProgress);
    }

    @GetMapping("/printers/dashboard-stats")
    public Map<String, Object> getDashboardStats() {
        return printerService.getDashboardStatistics();
    }

    @PostMapping("/printers/refresh/{id}")
    public void refreshSinglePrinter(@PathVariable Long id) {
        printerService.refreshSinglePrinter(id);
    }


    @DeleteMapping("/printers/delete/{id}")
    public void deletePrinter(@PathVariable Long id) {
        printerService.deleteSinglePrinter(id);
    }

    @PostMapping("/refreshPrintersList")
    public void refreshPrintersList(@RequestBody List<String> ipAddresses) {
        printerService.refreshPrintersByIpAddresses(ipAddresses);
    }
}