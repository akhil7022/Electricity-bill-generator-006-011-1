package com.voltgrid.model;

import java.time.LocalDateTime;

public class Customer {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String meterId;
    private ConnectionType connectionType;
    private LocalDateTime createdAt;

    public enum ConnectionType {
        RESIDENTIAL, COMMERCIAL, INDUSTRIAL
    }

    public Customer() {}

    public Customer(Integer id, String name, String email, String phone,
                    String address, String meterId, ConnectionType connectionType,
                    LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.meterId = meterId;
        this.connectionType = connectionType;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMeterId() { return meterId; }
    public void setMeterId(String meterId) { this.meterId = meterId; }

    public ConnectionType getConnectionType() { return connectionType; }
    public void setConnectionType(ConnectionType connectionType) { this.connectionType = connectionType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
