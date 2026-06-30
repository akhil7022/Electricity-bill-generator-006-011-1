# VoltGrid Swing — Java Desktop Application

A full Java Swing desktop GUI for the VoltGrid electricity billing system.
Connects directly to PostgreSQL via JDBC — no web server needed.

## Tech Stack

- **Java 17**
- **Java Swing** (standard Java GUI toolkit)
- **FlatLaf 3.4** — modern dark look and feel (no more grey 90s UI)
- **PostgreSQL JDBC 42.7** — direct database access
- **Maven** — build and dependency management

## Features

| Panel | What you can do |
|-------|----------------|
| 📊 Dashboard | Live stats: total customers, pending dues, revenue, bills this month + recent bills table |
| 👤 Customers | View all customers in a table; Add / Edit / Delete with dialog forms |
| 🔌 Meter Readings | Record new readings (auto-calculates units consumed); see billed status |
| 📄 Bills | Generate bills with slab tariff + 18% GST calculation; Mark PAID / OVERDUE |
| ⚙️ Tariffs | Add / delete slab pricing rules per connection type |

## Project Structure

```
java-swing/
├── pom.xml
└── src/main/java/com/voltgrid/swing/
    ├── VoltGridSwingApp.java        ← main() entry point
    ├── MainFrame.java               ← JFrame with sidebar + CardLayout
    ├── db/
    │   └── DatabaseHelper.java      ← singleton JDBC connection wrapper
    ├── panel/
    │   ├── Refreshable.java         ← interface: panels reload data when shown
    │   ├── DashboardPanel.java      ← stat cards + recent bills table
    │   ├── CustomersPanel.java      ← customer CRUD
    │   ├── MeterReadingsPanel.java  ← reading entry
    │   ├── BillsPanel.java          ← bill generation + status update
    │   └── TariffsPanel.java        ← tariff slab management
    └── util/
        ├── UIColors.java            ← color constants
        └── TableUtil.java           ← shared JTable helpers
```

## How to Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL running with the VoltGrid schema (`java-backend/src/main/resources/schema.sql`)

### 1. Set DATABASE_URL

```bash
export DATABASE_URL=postgresql://user:password@localhost:5432/voltgrid
```

The app accepts both forms:
- `postgresql://user:pass@host:port/db`
- `jdbc:postgresql://host:port/db?user=...&password=...`

### 2. Build

```bash
cd java-swing
mvn clean package -q
```

This produces a fat JAR at `target/voltgrid-swing-1.0.0.jar` (all dependencies bundled).

### 3. Run

```bash
java -jar target/voltgrid-swing-1.0.0.jar
```

A desktop window opens immediately. No browser needed.

## Bill Calculation Logic

Implemented in `BillsPanel.java` (mirrors `BillingService.java`):

1. Fetch tariff slabs for the customer's connection type (RESIDENTIAL / COMMERCIAL / INDUSTRIAL)
2. Apply slab rates progressively to units consumed
3. Add the fixed monthly charge
4. Apply **18% GST** on (energy charges + fixed charge)
5. Save bill with status `PENDING`; mark meter reading as billed

## Architecture Notes

- **`DatabaseHelper`** is a singleton — one JDBC connection for the whole app lifetime.
  It auto-reconnects if the connection drops.
- **`SwingWorker`** is used in every panel's `refresh()` method — database queries run
  on a background thread so the UI never freezes.
- **`Refreshable`** interface — `MainFrame` calls `refresh()` on the visible panel
  whenever the user switches tabs, so data is always up to date.
- **FlatLaf** replaces Java's default Metal look and feel with a modern dark theme
  with zero configuration — just call `FlatDarkPurpleIJTheme.setup()` before any UI code.
