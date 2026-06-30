-- VoltGrid Billing System — PostgreSQL Schema
-- Run this to initialize the database

CREATE TYPE IF NOT EXISTS connection_type AS ENUM ('RESIDENTIAL', 'COMMERCIAL', 'INDUSTRIAL');
CREATE TYPE IF NOT EXISTS bill_status AS ENUM ('PENDING', 'PAID', 'OVERDUE');

CREATE TABLE IF NOT EXISTS customers (
    id           SERIAL PRIMARY KEY,
    name         TEXT NOT NULL,
    email        TEXT NOT NULL,
    phone        TEXT NOT NULL,
    address      TEXT NOT NULL,
    meter_id     TEXT NOT NULL UNIQUE,
    connection_type connection_type NOT NULL,
    created_at   TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS tariffs (
    id              SERIAL PRIMARY KEY,
    name            TEXT NOT NULL,
    units_from      INTEGER NOT NULL,
    units_to        INTEGER,
    rate_per_unit   NUMERIC(10, 4) NOT NULL,
    fixed_charge    NUMERIC(10, 2) NOT NULL,
    connection_type connection_type NOT NULL
);

CREATE TABLE IF NOT EXISTS meter_readings (
    id               SERIAL PRIMARY KEY,
    customer_id      INTEGER NOT NULL REFERENCES customers(id),
    previous_reading NUMERIC(10, 2) NOT NULL,
    current_reading  NUMERIC(10, 2) NOT NULL,
    units_consumed   NUMERIC(10, 2) NOT NULL,
    reading_date     DATE NOT NULL,
    billing_month    TEXT NOT NULL,
    billed           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS bills (
    id               SERIAL PRIMARY KEY,
    bill_number      TEXT NOT NULL UNIQUE,
    customer_id      INTEGER NOT NULL REFERENCES customers(id),
    meter_reading_id INTEGER NOT NULL REFERENCES meter_readings(id),
    units_consumed   NUMERIC(10, 2) NOT NULL,
    energy_charges   NUMERIC(10, 2) NOT NULL,
    fixed_charges    NUMERIC(10, 2) NOT NULL,
    tax_amount       NUMERIC(10, 2) NOT NULL,
    total_amount     NUMERIC(10, 2) NOT NULL,
    billing_month    TEXT NOT NULL,
    due_date         DATE NOT NULL,
    status           bill_status NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP DEFAULT NOW() NOT NULL
);
