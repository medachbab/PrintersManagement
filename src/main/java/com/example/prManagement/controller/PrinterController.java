package com.example.prManagement.controller;

import com.example.prManagement.model.Printer;
import com.example.prManagement.service.PrinterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class PrinterController {

    @Autowired
    private PrinterService printerService;

    @GetMapping("/printers")
    public String listPrinters(
            Model model,
            @RequestParam(required = false) String nameSearch,
            @RequestParam(required = false) Integer minToner,
            @RequestParam(required = false) Integer minPages) {

        // This call will now update the lastRefreshTime *within each Printer object* in the database
        printerService.getAllAndUpdatePrinters();

        List<Printer> filteredPrinters = printerService.getFilteredPrinters(nameSearch, minToner, minPages);

        model.addAttribute("printers", filteredPrinters);
        model.addAttribute("nameSearch", nameSearch);
        model.addAttribute("minToner", minToner);
        model.addAttribute("minPages", minPages);

        // Removed: model.addAttribute("lastRefreshTime", printerService.getLastRefreshTime());
        // This is no longer needed as last refresh time is part of each printer object.

        return "printers";
    }

    @PostMapping("/discoverPrinters")
    public String discoverPrinters(RedirectAttributes redirectAttributes) {
        String subnetPrefix = "127.0.0."; // Adjust this to your network
        int startIp = 1;
        int endIp = 254;

        printerService.discoverAndAddPrinters(subnetPrefix, startIp, endIp);

        redirectAttributes.addFlashAttribute("message", "Discovery started. Please wait...");
        return "redirect:/printers";
    }

    @GetMapping("/discoveryProgress")
    @ResponseBody
    public Map<String, Object> getDiscoveryProgress() {
        int progress = printerService.getDiscoveryProgressPercentage();
        boolean inProgress = printerService.isDiscoveryInProgress();
        return Map.of("progress", progress, "inProgress", inProgress);
    }
}
/*
// src/main/java/com/example/prManagement/controller/PrinterController.java
package com.example.prManagement.controller;

import com.example.prManagement.model.Printer;
import com.example.prManagement.service.PrinterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody; // Import for JSON response
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map; // Import for Map

@Controller
public class PrinterController {

    @Autowired
    private PrinterService printerService;

    @GetMapping("/printers")
    public String listPrinters(
            Model model,
            @RequestParam(required = false) String nameSearch,
            @RequestParam(required = false) Integer minToner,
            @RequestParam(required = false) Integer minPages) {

        // Always update all printer details from SNMP when viewing the page
        printerService.getAllAndUpdatePrinters();

        // Then, retrieve and filter the printers based on search/filter parameters
        List<Printer> filteredPrinters = printerService.getFilteredPrinters(nameSearch, minToner, minPages);

        model.addAttribute("printers", filteredPrinters);
        model.addAttribute("nameSearch", nameSearch);
        model.addAttribute("minToner", minToner);
        model.addAttribute("minPages", minPages);

        return "printers";
    }

    @PostMapping("/discoverPrinters")
    public String discoverPrinters(RedirectAttributes redirectAttributes) {
        // Adjust these values to your network subnet (e.g., from ipconfig)
        String subnetPrefix = "127.0.0."; // From your ipconfig: 192.168.3.133
        int startIp = 1;
        int endIp = 254; // Standard /24 range

        // Start discovery asynchronously
        printerService.discoverAndAddPrinters(subnetPrefix, startIp, endIp);

        redirectAttributes.addFlashAttribute("message", "Discovery started. Please wait...");
        return "redirect:/printers";
    }

    // New REST endpoint to get discovery progress
    @GetMapping("/discoveryProgress")
    @ResponseBody // This tells Spring to return the data directly as JSON (or XML etc.)
    public Map<String, Object> getDiscoveryProgress() {
        int progress = printerService.getDiscoveryProgressPercentage();
        boolean inProgress = printerService.isDiscoveryInProgress();
        return Map.of("progress", progress, "inProgress", inProgress);
    }
}*/
