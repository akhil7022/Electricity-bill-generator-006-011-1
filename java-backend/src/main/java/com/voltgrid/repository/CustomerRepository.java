package com.voltgrid.repository;

import com.voltgrid.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbc;

    public CustomerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Customer> rowMapper = (rs, rowNum) -> {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setMeterId(rs.getString("meter_id"));
        c.setConnectionType(Customer.ConnectionType.valueOf(rs.getString("connection_type")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    };

    public List<Customer> findAll() {
        return jdbc.query("SELECT * FROM customers ORDER BY id", rowMapper);
    }

    public Optional<Customer> findById(int id) {
        List<Customer> results = jdbc.query(
            "SELECT * FROM customers WHERE id = ?", rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Customer save(Customer customer) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO customers (name, email, phone, address, meter_id, connection_type) " +
                "VALUES (?, ?, ?, ?, ?, ?::connection_type)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getMeterId());
            ps.setString(6, customer.getConnectionType().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return findById(key != null ? key.intValue() : 0).orElseThrow();
    }

    public Optional<Customer> update(int id, Customer customer) {
        int rows = jdbc.update(
            "UPDATE customers SET name=?, email=?, phone=?, address=?, meter_id=?, connection_type=?::connection_type WHERE id=?",
            customer.getName(), customer.getEmail(), customer.getPhone(),
            customer.getAddress(), customer.getMeterId(),
            customer.getConnectionType().name(), id);
        if (rows == 0) return Optional.empty();
        return findById(id);
    }

    public boolean delete(int id) {
        return jdbc.update("DELETE FROM customers WHERE id = ?", id) > 0;
    }

    public long count() {
        Long result = jdbc.queryForObject("SELECT COUNT(*) FROM customers", Long.class);
        return result != null ? result : 0L;
    }
}
