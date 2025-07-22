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
    public String discoverPrinters(RedirectAttributes redirectAttributes) {
        String subnetPrefix = "127.0.0.";//this network will be adjusted based on the production network
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
    public String refreshSinglePrinter(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        printerService.refreshSinglePrinter(id);
        redirectAttributes.addFlashAttribute("message", "Printer with ID " + id + " data refreshed.");
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