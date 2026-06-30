package com.voltgrid.swing.db;

import java.sql.*;

/**
 * Singleton that holds one JDBC Connection to PostgreSQL.
 * Reads the DATABASE_URL environment variable (same as the Node.js and Java backends).
 *
 * DATABASE_URL format expected by this helper:
 *   postgresql://user:password@host:port/dbname
 * or the full JDBC form:
 *   jdbc:postgresql://host:port/dbname?user=...&password=...
 */
public class DatabaseHelper {

    private static DatabaseHelper instance;
    private Connection connection;

    private DatabaseHelper() throws SQLException {
        String url = System.getenv("DATABASE_URL");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set.");
        }

        // Convert postgres:// → jdbc:postgresql:// if needed
        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            url = "jdbc:postgresql" + url.substring(url.indexOf("://"));
        }

        connection = DriverManager.getConnection(url);
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            try {
                instance = new DatabaseHelper();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
            }
        }
        // Re-connect if connection dropped
        try {
            if (instance.connection == null || instance.connection.isClosed()) {
                instance = new DatabaseHelper();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection check failed", e);
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    /** Execute a query and pass the ResultSet to a handler lambda. */
    public <T> T query(String sql, ResultSetHandler<T> handler, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return handler.handle(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + sql, e);
        }
    }

    /** Execute an INSERT/UPDATE/DELETE and return affected row count. */
    public int update(String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update failed: " + sql, e);
        }
    }

    /** Execute INSERT and return the generated key. */
    public int insert(String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed: " + sql, e);
        }
        return -1;
    }

    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(ResultSet rs) throws SQLException;
    }
}
