package com.voltgrid.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Bill {
    private Integer id;
    private String billNumber;
    private Integer customerId;
    private String customerName;
    private String meterId;
    private Integer meterReadingId;
    private BigDecimal unitsConsumed;
    private BigDecimal energyCharges;
    private BigDecimal fixedCharges;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String billingMonth;
    private LocalDate dueDate;
    private BillStatus status;
    private LocalDateTime createdAt;

    public enum BillStatus {
        PENDING, PAID, OVERDUE
    }

    public Bill() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getMeterId() { return meterId; }
    public void setMeterId(String meterId) { this.meterId = meterId; }

    public Integer getMeterReadingId() { return meterReadingId; }
    public void setMeterReadingId(Integer meterReadingId) { this.meterReadingId = meterReadingId; }

    public BigDecimal getUnitsConsumed() { return unitsConsumed; }
    public void setUnitsConsumed(BigDecimal unitsConsumed) { this.unitsConsumed = unitsConsumed; }

    public BigDecimal getEnergyCharges() { return energyCharges; }
    public void setEnergyCharges(BigDecimal energyCharges) { this.energyCharges = energyCharges; }

    public BigDecimal getFixedCharges() { return fixedCharges; }
    public void setFixedCharges(BigDecimal fixedCharges) { this.fixedCharges = fixedCharges; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BillStatus getStatus() { return status; }
    public void setStatus(BillStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
