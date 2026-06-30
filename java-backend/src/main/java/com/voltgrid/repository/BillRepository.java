package com.voltgrid.repository;

import com.voltgrid.model.Bill;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BillRepository {

    private final JdbcTemplate jdbc;

    public BillRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_WITH_CUSTOMER =
        "SELECT b.*, c.name AS customer_name, c.meter_id " +
        "FROM bills b " +
        "JOIN customers c ON c.id = b.customer_id ";

    private final RowMapper<Bill> rowMapper = (rs, rowNum) -> {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setBillNumber(rs.getString("bill_number"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setMeterId(rs.getString("meter_id"));
        b.setMeterReadingId(rs.getInt("meter_reading_id"));
        b.setUnitsConsumed(rs.getBigDecimal("units_consumed"));
        b.setEnergyCharges(rs.getBigDecimal("energy_charges"));
        b.setFixedCharges(rs.getBigDecimal("fixed_charges"));
        b.setTaxAmount(rs.getBigDecimal("tax_amount"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setBillingMonth(rs.getString("billing_month"));
        Date d = rs.getDate("due_date");
        if (d != null) b.setDueDate(d.toLocalDate());
        b.setStatus(Bill.BillStatus.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
        return b;
    };

    public List<Bill> findAll(Integer customerId, String status) {
        StringBuilder sql = new StringBuilder(SELECT_WITH_CUSTOMER);
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (customerId != null) {
            conditions.add("b.customer_id = ?");
            params.add(customerId);
        }
        if (status != null && !status.isEmpty()) {
            conditions.add("b.status = ?::bill_status");
            params.add(status);
        }
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }
        sql.append("ORDER BY b.id");
        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<Bill> findById(int id) {
        List<Bill> results = jdbc.query(
            SELECT_WITH_CUSTOMER + "WHERE b.id = ?", rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Bill save(Bill bill) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO bills (bill_number, customer_id, meter_reading_id, units_consumed, " +
                "energy_charges, fixed_charges, tax_amount, total_amount, billing_month, due_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::bill_status)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, bill.getBillNumber());
            ps.setInt(2, bill.getCustomerId());
            ps.setInt(3, bill.getMeterReadingId());
            ps.setBigDecimal(4, bill.getUnitsConsumed());
            ps.setBigDecimal(5, bill.getEnergyCharges());
            ps.setBigDecimal(6, bill.getFixedCharges());
            ps.setBigDecimal(7, bill.getTaxAmount());
            ps.setBigDecimal(8, bill.getTotalAmount());
            ps.setString(9, bill.getBillingMonth());
            ps.setDate(10, Date.valueOf(bill.getDueDate()));
            ps.setString(11, bill.getStatus().name());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return findById(key != null ? key.intValue() : 0).orElseThrow();
    }

    public Optional<Bill> updateStatus(int id, Bill.BillStatus status) {
        int rows = jdbc.update(
            "UPDATE bills SET status = ?::bill_status WHERE id = ?", status.name(), id);
        if (rows == 0) return Optional.empty();
        return findById(id);
    }

    public long count() {
        Long result = jdbc.queryForObject("SELECT COUNT(*) FROM bills", Long.class);
        return result != null ? result : 0L;
    }

    public BigDecimal sumByStatus(Bill.BillStatus status) {
        BigDecimal result = jdbc.queryForObject(
            "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE status = ?::bill_status",
            BigDecimal.class, status.name());
        return result != null ? result : BigDecimal.ZERO;
    }

    public long countThisMonth() {
        Long result = jdbc.queryForObject(
            "SELECT COUNT(*) FROM bills WHERE TO_CHAR(created_at, 'YYYY-MM') = TO_CHAR(NOW(), 'YYYY-MM')",
            Long.class);
        return result != null ? result : 0L;
    }

    public List<Bill> findRecent(int limit) {
        return jdbc.query(
            SELECT_WITH_CUSTOMER + "ORDER BY b.created_at DESC LIMIT ?",
            rowMapper, limit);
    }
}
