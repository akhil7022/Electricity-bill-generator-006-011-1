package com.voltgrid.model;

import java.math.BigDecimal;

public class Tariff {
    private Integer id;
    private String name;
    private Integer unitsFrom;
    private Integer unitsTo;
    private BigDecimal ratePerUnit;
    private BigDecimal fixedCharge;
    private Customer.ConnectionType connectionType;

    public Tariff() {}

    public Tariff(Integer id, String name, Integer unitsFrom, Integer unitsTo,
                  BigDecimal ratePerUnit, BigDecimal fixedCharge,
                  Customer.ConnectionType connectionType) {
        this.id = id;
        this.name = name;
        this.unitsFrom = unitsFrom;
        this.unitsTo = unitsTo;
        this.ratePerUnit = ratePerUnit;
        this.fixedCharge = fixedCharge;
        this.connectionType = connectionType;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getUnitsFrom() { return unitsFrom; }
    public void setUnitsFrom(Integer unitsFrom) { this.unitsFrom = unitsFrom; }

    public Integer getUnitsTo() { return unitsTo; }
    public void setUnitsTo(Integer unitsTo) { this.unitsTo = unitsTo; }

    public BigDecimal getRatePerUnit() { return ratePerUnit; }
    public void setRatePerUnit(BigDecimal ratePerUnit) { this.ratePerUnit = ratePerUnit; }

    public BigDecimal getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }

    public Customer.ConnectionType getConnectionType() { return connectionType; }
    public void setConnectionType(Customer.ConnectionType connectionType) { this.connectionType = connectionType; }
}
