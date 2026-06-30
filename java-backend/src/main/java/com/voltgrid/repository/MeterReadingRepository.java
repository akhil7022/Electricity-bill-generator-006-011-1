package com.voltgrid.repository;

import com.voltgrid.model.MeterReading;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class MeterReadingRepository {

    private final JdbcTemplate jdbc;

    public MeterReadingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_WITH_CUSTOMER =
        "SELECT mr.*, c.name AS customer_name, c.meter_id " +
        "FROM meter_readings mr " +
        "JOIN customers c ON c.id = mr.customer_id ";

    private final RowMapper<MeterReading> rowMapper = (rs, rowNum) -> {
        MeterReading mr = new MeterReading();
        mr.setId(rs.getInt("id"));
        mr.setCustomerId(rs.getInt("customer_id"));
        mr.setCustomerName(rs.getString("customer_name"));
        mr.setMeterId(rs.getString("meter_id"));
        mr.setPreviousReading(rs.getBigDecimal("previous_reading"));
        mr.setCurrentReading(rs.getBigDecimal("current_reading"));
        mr.setUnitsConsumed(rs.getBigDecimal("units_consumed"));
        Date d = rs.getDate("reading_date");
        if (d != null) mr.setReadingDate(d.toLocalDate());
        mr.setBillingMonth(rs.getString("billing_month"));
        mr.setBilled(rs.getBoolean("billed"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) mr.setCreatedAt(ts.toLocalDateTime());
        return mr;
    };

    public List<MeterReading> findAll() {
        return jdbc.query(SELECT_WITH_CUSTOMER + "ORDER BY mr.id", rowMapper);
    }

    public List<MeterReading> findByCustomerId(int customerId) {
        return jdbc.query(SELECT_WITH_CUSTOMER + "WHERE mr.customer_id = ? ORDER BY mr.id",
            rowMapper, customerId);
    }

    public Optional<MeterReading> findById(int id) {
        List<MeterReading> results = jdbc.query(
            SELECT_WITH_CUSTOMER + "WHERE mr.id = ?", rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public MeterReading save(MeterReading mr) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO meter_readings (customer_id, previous_reading, current_reading, " +
                "units_consumed, reading_date, billing_month, billed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, mr.getCustomerId());
            ps.setBigDecimal(2, mr.getPreviousReading());
            ps.setBigDecimal(3, mr.getCurrentReading());
            ps.setBigDecimal(4, mr.getUnitsConsumed());
            ps.setDate(5, Date.valueOf(mr.getReadingDate()));
            ps.setString(6, mr.getBillingMonth());
            ps.setBoolean(7, mr.getBilled() != null ? mr.getBilled() : false);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return findById(key != null ? key.intValue() : 0).orElseThrow();
    }

    public void markBilled(int id) {
        jdbc.update("UPDATE meter_readings SET billed = TRUE WHERE id = ?", id);
    }
}
