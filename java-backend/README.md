# VoltGrid Billing System — Java Backend

Electricity billing management system built with **Java 17 + Spring Boot + JDBC + PostgreSQL**.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2** (Web + JDBC — no JPA/Hibernate, raw JDBC only)
- **PostgreSQL** (via JDBC `JdbcTemplate`)
- **Maven** build tool

## Architecture

- `model/` — Plain Java POJOs (Customer, Tariff, MeterReading, Bill)
- `repository/` — Raw JDBC repositories using `JdbcTemplate`
- `service/` — Business logic (slab tariff calculation, GST, bill generation)
- `controller/` — REST API controllers

## Features

- **Customer management** — CRUD for electricity consumers (RESIDENTIAL / COMMERCIAL / INDUSTRIAL)
- **Tariff management** — Configure slab-based tariff rates per connection type
- **Meter readings** — Record current and previous readings; auto-calculates units consumed
- **Bill generation** — Auto-calculates energy charges (slab tariff), fixed charges, 18% GST
- **Dashboard** — Summary stats: total customers, pending dues, revenue, bills this month

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/summary` | Dashboard statistics |
| GET/POST | `/api/customers` | List / create customers |
| GET/PUT/DELETE | `/api/customers/{id}` | Get / update / delete customer |
| GET/POST | `/api/tariffs` | List / create tariff slabs |
| PUT/DELETE | `/api/tariffs/{id}` | Update / delete tariff |
| GET/POST | `/api/meter-readings` | List / record meter readings |
| GET | `/api/meter-readings/{id}` | Get meter reading |
| GET/POST | `/api/bills` | List / generate bills |
| GET | `/api/bills/{id}` | Get bill detail |
| PATCH | `/api/bills/{id}` | Update bill status (PENDING/PAID/OVERDUE) |

## Setup & Run

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL running

### 1. Initialize the database

```sql
-- Run the schema file
psql -U your_user -d your_db -f src/main/resources/schema.sql
```

### 2. Configure environment

Set the `DATABASE_URL` environment variable:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/voltgrid?user=youruser&password=yourpassword
```

Or edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/voltgrid
spring.datasource.username=youruser
spring.datasource.password=yourpassword
```

### 3. Build & run

```bash
mvn clean package
java -jar target/voltgrid-billing-1.0.0.jar
```

Or for development:

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

## Bill Calculation Logic

Bills are calculated in `BillingService.java` using raw JDBC:

1. Fetch the customer's connection type
2. Query tariff slabs for that connection type (ordered by `units_from`)
3. Apply slab-based rates to units consumed
4. Add fixed charge
5. Apply 18% GST on (energy + fixed charges)
6. Total = energy charges + fixed charges + GST

## Database Schema

See `src/main/resources/schema.sql` for the complete PostgreSQL schema with ENUMs and foreign keys.
