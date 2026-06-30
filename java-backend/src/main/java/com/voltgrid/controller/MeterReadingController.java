package com.voltgrid.controller;

import com.voltgrid.model.MeterReading;
import com.voltgrid.repository.CustomerRepository;
import com.voltgrid.repository.MeterReadingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meter-readings")
@CrossOrigin(origins = "*")
public class MeterReadingController {

    private final MeterReadingRepository meterReadingRepo;
    private final CustomerRepository customerRepo;

    public MeterReadingController(MeterReadingRepository meterReadingRepo,
                                   CustomerRepository customerRepo) {
        this.meterReadingRepo = meterReadingRepo;
        this.customerRepo = customerRepo;
    }

    @GetMapping
    public List<MeterReading> getAll(@RequestParam(required = false) Integer customerId) {
        if (customerId != null) return meterReadingRepo.findByCustomerId(customerId);
        return meterReadingRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeterReading> getById(@PathVariable int id) {
        return meterReadingRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            int customerId = ((Number) body.get("customerId")).intValue();
            BigDecimal previous = new BigDecimal(body.get("previousReading").toString());
            BigDecimal current = new BigDecimal(body.get("currentReading").toString());
            String readingDate = body.get("readingDate").toString();
            String billingMonth = body.get("billingMonth").toString();

            if (current.compareTo(previous) < 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Current reading must be >= previous reading"));
            }

            MeterReading mr = new MeterReading();
            mr.setCustomerId(customerId);
            mr.setPreviousReading(previous);
            mr.setCurrentReading(current);
            mr.setUnitsConsumed(current.subtract(previous));
            mr.setReadingDate(java.time.LocalDate.parse(readingDate));
            mr.setBillingMonth(billingMonth);
            mr.setBilled(false);

            MeterReading saved = meterReadingRepo.save(mr);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
