package com.voltgrid.controller;

import com.voltgrid.service.BillingService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final BillingService billingService;

    public DashboardController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return billingService.getDashboardSummary();
    }
}
