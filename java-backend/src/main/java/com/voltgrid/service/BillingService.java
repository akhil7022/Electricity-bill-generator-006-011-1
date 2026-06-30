package com.voltgrid.service;

import com.voltgrid.model.*;
import com.voltgrid.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class BillingService {

    private final CustomerRepository customerRepo;
    private final TariffRepository tariffRepo;
    private final MeterReadingRepository meterReadingRepo;
    private final BillRepository billRepo;

    public BillingService(CustomerRepository customerRepo, TariffRepository tariffRepo,
                          MeterReadingRepository meterReadingRepo, BillRepository billRepo) {
        this.customerRepo = customerRepo;
        this.tariffRepo = tariffRepo;
        this.meterReadingRepo = meterReadingRepo;
        this.billRepo = billRepo;
    }

    public Bill generateBill(int customerId, int meterReadingId, LocalDate dueDate) {
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        MeterReading reading = meterReadingRepo.findById(meterReadingId)
            .orElseThrow(() -> new IllegalArgumentException("Meter reading not found: " + meterReadingId));

        BigDecimal units = reading.getUnitsConsumed();

        // Calculate energy charges using slab tariffs via JDBC
        List<Tariff> tariffs = tariffRepo.findByConnectionType(customer.getConnectionType());

        BigDecimal energyCharges = BigDecimal.ZERO;
        BigDecimal fixedCharges = BigDecimal.ZERO;

        if (tariffs.isEmpty()) {
            // Default rate if no tariff defined
            energyCharges = units.multiply(new BigDecimal("5.00"));
            fixedCharges = new BigDecimal("50.00");
        } else {
            BigDecimal remaining = units;
            for (Tariff slab : tariffs) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal slabMax;
                if (slab.getUnitsTo() != null) {
                    slabMax = new BigDecimal(slab.getUnitsTo() - slab.getUnitsFrom());
                } else {
                    slabMax = remaining; // unbounded top slab
                }
                BigDecimal slabUnits = remaining.min(slabMax);
                energyCharges = energyCharges.add(slabUnits.multiply(slab.getRatePerUnit()));
                remaining = remaining.subtract(slabUnits);
            }
            fixedCharges = tariffs.get(0).getFixedCharge();
        }

        // 18% GST
        BigDecimal taxBase = energyCharges.add(fixedCharges);
        BigDecimal taxAmount = taxBase.multiply(new BigDecimal("0.18"))
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = taxBase.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        // Generate bill number
        long count = billRepo.count();
        String billNumber = String.format("#%06d", count + 1);

        Bill bill = new Bill();
        bill.setBillNumber(billNumber);
        bill.setCustomerId(customerId);
        bill.setMeterReadingId(meterReadingId);
        bill.setUnitsConsumed(units);
        bill.setEnergyCharges(energyCharges.setScale(2, RoundingMode.HALF_UP));
        bill.setFixedCharges(fixedCharges.setScale(2, RoundingMode.HALF_UP));
        bill.setTaxAmount(taxAmount);
        bill.setTotalAmount(totalAmount);
        bill.setBillingMonth(reading.getBillingMonth());
        bill.setDueDate(dueDate);
        bill.setStatus(Bill.BillStatus.PENDING);

        Bill saved = billRepo.save(bill);
        meterReadingRepo.markBilled(meterReadingId);
        return saved;
    }

    public Map<String, Object> getDashboardSummary() {
        long totalCustomers = customerRepo.count();
        BigDecimal pendingDues = billRepo.sumByStatus(Bill.BillStatus.PENDING);
        BigDecimal totalRevenue = billRepo.sumByStatus(Bill.BillStatus.PAID);
        long billsThisMonth = billRepo.countThisMonth();
        List<Bill> recentBills = billRepo.findRecent(5);

        return Map.of(
            "totalCustomers", totalCustomers,
            "pendingDues", pendingDues,
            "totalRevenue", totalRevenue,
            "billsThisMonth", billsThisMonth,
            "recentBills", recentBills
        );
    }
}
