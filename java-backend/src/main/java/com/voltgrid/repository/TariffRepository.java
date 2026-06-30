package com.voltgrid.repository;

import com.voltgrid.model.Customer;
import com.voltgrid.model.Tariff;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class TariffRepository {

    private final JdbcTemplate jdbc;

    public TariffRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Tariff> rowMapper = (rs, rowNum) -> {
        Tariff t = new Tariff();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setUnitsFrom(rs.getInt("units_from"));
        int unitsTo = rs.getInt("units_to");
        t.setUnitsTo(rs.wasNull() ? null : unitsTo);
        t.setRatePerUnit(rs.getBigDecimal("rate_per_unit"));
        t.setFixedCharge(rs.getBigDecimal("fixed_charge"));
        t.setConnectionType(Customer.ConnectionType.valueOf(rs.getString("connection_type")));
        return t;
    };

    public List<Tariff> findAll() {
        return jdbc.query("SELECT * FROM tariffs ORDER BY connection_type, units_from", rowMapper);
    }

    public List<Tariff> findByConnectionType(Customer.ConnectionType type) {
        return jdbc.query(
            "SELECT * FROM tariffs WHERE connection_type = ?::connection_type ORDER BY units_from",
            rowMapper, type.name());
    }

    public Optional<Tariff> findById(int id) {
        List<Tariff> results = jdbc.query("SELECT * FROM tariffs WHERE id = ?", rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Tariff save(Tariff tariff) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO tariffs (name, units_from, units_to, rate_per_unit, fixed_charge, connection_type) " +
                "VALUES (?, ?, ?, ?, ?, ?::connection_type)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tariff.getName());
            ps.setInt(2, tariff.getUnitsFrom());
            if (tariff.getUnitsTo() != null) ps.setInt(3, tariff.getUnitsTo());
            else ps.setNull(3, Types.INTEGER);
            ps.setBigDecimal(4, tariff.getRatePerUnit());
            ps.setBigDecimal(5, tariff.getFixedCharge());
            ps.setString(6, tariff.getConnectionType().name());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return findById(key != null ? key.intValue() : 0).orElseThrow();
    }

    public Optional<Tariff> update(int id, Tariff tariff) {
        int rows = jdbc.update(
            "UPDATE tariffs SET name=?, units_from=?, units_to=?, rate_per_unit=?, fixed_charge=?, connection_type=?::connection_type WHERE id=?",
            tariff.getName(), tariff.getUnitsFrom(), tariff.getUnitsTo(),
            tariff.getRatePerUnit(), tariff.getFixedCharge(),
            tariff.getConnectionType().name(), id);
        if (rows == 0) return Optional.empty();
        return findById(id);
    }

    public boolean delete(int id) {
        return jdbc.update("DELETE FROM tariffs WHERE id = ?", id) > 0;
    }
}
