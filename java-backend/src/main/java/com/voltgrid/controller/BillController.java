package com.voltgrid.controller;

import com.voltgrid.model.Bill;
import com.voltgrid.repository.BillRepository;
import com.voltgrid.service.BillingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*")
public class BillController {

    private final BillRepository billRepo;
    private final BillingService billingService;

    public BillController(BillRepository billRepo, BillingService billingService) {
        this.billRepo = billRepo;
        this.billingService = billingService;
    }

    @GetMapping
    public List<Bill> getAll(
        @RequestParam(required = false) Integer customerId,
        @RequestParam(required = false) String status) {
        return billRepo.findAll(customerId, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getById(@PathVariable int id) {
        return billRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> generate(@RequestBody Map<String, Object> body) {
        try {
            int customerId = ((Number) body.get("customerId")).intValue();
            int meterReadingId = ((Number) body.get("meterReadingId")).intValue();
            LocalDate dueDate = LocalDate.parse(body.get("dueDate").toString());
            Bill bill = billingService.generateBill(customerId, meterReadingId, dueDate);
            return ResponseEntity.status(HttpStatus.CREATED).body(bill);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        try {
            Bill.BillStatus status = Bill.BillStatus.valueOf(body.get("status"));
            return billRepo.updateStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        }
    }
}
