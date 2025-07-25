package com.example.prManagement.controller;

import com.example.prManagement.model.Printer;
import com.example.prManagement.service.PrinterService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class PrinterController {

    @Autowired
    private PrinterService printerService;

    @GetMapping("/printers")
    public String listPrinters(
            Model model,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "name") String searchType,
            @RequestParam(required = false, defaultValue = "none") String filterType,
            @RequestParam(required = false) Integer minToner,
            @RequestParam(required = false) Integer minPages) {

        List<Printer> filteredPrinters = printerService.getFilteredPrinters(searchTerm, searchType, filterType, minToner, minPages);

        model.addAttribute("printers", filteredPrinters);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("searchType", searchType);
        model.addAttribute("filterType", filterType);
        model.addAttribute("minToner", minToner);
        model.addAttribute("minPages", minPages);

        return "printers";
    }

    @PostMapping("/discoverPrinters")
    public String discoverPrinters(@RequestParam String subnetPrefix, RedirectAttributes redirectAttributes) {
        int startIp = 1;
        int endIp = 254;

        printerService.discoverAndAddPrinters(subnetPrefix, startIp, endIp);

        redirectAttributes.addFlashAttribute("message", "Discovery started. Please wait...");
        return "redirect:/printers";
    }

    @PostMapping("/refreshPrinters")
    public String refreshPrinters(RedirectAttributes redirectAttributes) {
        printerService.getAllAndUpdatePrinters();
        redirectAttributes.addFlashAttribute("message", "All printer data refresh started. It may take some time.");
        return "redirect:/printers";
    }

    @PostMapping("/refreshPrinter")
    public String refreshSinglePrinter(
            @RequestParam Long id,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "name") String searchType,
            @RequestParam(required = false, defaultValue = "none") String filterType,
            @RequestParam(required = false) Integer minToner,
            @RequestParam(required = false) Integer minPages,
            RedirectAttributes redirectAttributes) {

        printerService.refreshSinglePrinter(id);
        redirectAttributes.addFlashAttribute("message", "Printer with ID " + id + " data refreshed.");

        // Build the redirect URL with existing filter parameters
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/printers");
        if (searchTerm != null && !searchTerm.isEmpty()) {
            uriBuilder.queryParam("searchTerm", searchTerm);
        }
        uriBuilder.queryParam("searchType", searchType);
        if (!"none".equals(filterType)) {
            uriBuilder.queryParam("filterType", filterType);
        }
        if (minToner != null) {
            uriBuilder.queryParam("minToner", minToner);
        }
        if (minPages != null) {
            uriBuilder.queryParam("minPages", minPages);
        }

        return "redirect:" + uriBuilder.toUriString();
    }

    @GetMapping("/discoveryProgress")
    @ResponseBody
    public Map<String, Object> getDiscoveryProgress() {
        int progress = printerService.getDiscoveryProgressPercentage();
        boolean inProgress = printerService.isDiscoveryInProgress();
        return Map.of("progress", progress, "inProgress", inProgress);
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
}