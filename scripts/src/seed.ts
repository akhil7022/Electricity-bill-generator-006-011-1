import { db } from "@workspace/db";
import { customersTable, tariffsTable, meterReadingsTable, billsTable } from "@workspace/db/schema";
import { count } from "drizzle-orm";

async function seed() {
  const [existing] = await db.select({ cnt: count() }).from(customersTable);
  if ((existing?.cnt ?? 0) > 0) {
    console.log("Database already seeded, skipping.");
    process.exit(0);
  }

  console.log("Seeding database...");

  // Tariffs
  await db.insert(tariffsTable).values([
    { name: "Residential 0-100", unitsFrom: 0, unitsTo: 100, ratePerUnit: "2.50", fixedCharge: "50.00", connectionType: "RESIDENTIAL" },
    { name: "Residential 101-300", unitsFrom: 101, unitsTo: 300, ratePerUnit: "4.00", fixedCharge: "50.00", connectionType: "RESIDENTIAL" },
    { name: "Residential 300+", unitsFrom: 301, unitsTo: null, ratePerUnit: "6.00", fixedCharge: "50.00", connectionType: "RESIDENTIAL" },
    { name: "Commercial 0-500", unitsFrom: 0, unitsTo: 500, ratePerUnit: "6.50", fixedCharge: "200.00", connectionType: "COMMERCIAL" },
    { name: "Commercial 500+", unitsFrom: 501, unitsTo: null, ratePerUnit: "8.00", fixedCharge: "200.00", connectionType: "COMMERCIAL" },
    { name: "Industrial", unitsFrom: 0, unitsTo: null, ratePerUnit: "5.00", fixedCharge: "500.00", connectionType: "INDUSTRIAL" },
  ]);

  // Customers
  const customers = await db.insert(customersTable).values([
    { name: "Ravi Kumar", email: "ravi@example.com", phone: "9876543210", address: "12 MG Road, Bangalore", meterId: "MTR-001", connectionType: "RESIDENTIAL" },
    { name: "Priya Sharma", email: "priya@example.com", phone: "9876543211", address: "45 Park Street, Mumbai", meterId: "MTR-002", connectionType: "RESIDENTIAL" },
    { name: "Shankar Das", email: "shankar@example.com", phone: "9876543212", address: "8 Nehru Nagar, Delhi", meterId: "MTR-003", connectionType: "COMMERCIAL" },
    { name: "Sri Lakshmi Textiles", email: "textiles@example.com", phone: "9876543213", address: "22 Industrial Zone, Chennai", meterId: "MTR-004", connectionType: "INDUSTRIAL" },
    { name: "Anand Raj", email: "anand@example.com", phone: "9876543214", address: "67 Anna Salai, Hyderabad", meterId: "MTR-005", connectionType: "RESIDENTIAL" },
  ]).returning();

  // Meter readings
  const readings = await db.insert(meterReadingsTable).values([
    { customerId: customers[0].id, previousReading: "0", currentReading: "120", unitsConsumed: "120", readingDate: "2026-06-01", billingMonth: "June 2026", billed: false },
    { customerId: customers[1].id, previousReading: "200", currentReading: "460", unitsConsumed: "260", readingDate: "2026-06-01", billingMonth: "June 2026", billed: false },
    { customerId: customers[2].id, previousReading: "1000", currentReading: "1850", unitsConsumed: "850", readingDate: "2026-06-01", billingMonth: "June 2026", billed: false },
    { customerId: customers[3].id, previousReading: "5000", currentReading: "7200", unitsConsumed: "2200", readingDate: "2026-06-01", billingMonth: "June 2026", billed: false },
    { customerId: customers[4].id, previousReading: "300", currentReading: "380", unitsConsumed: "80", readingDate: "2026-06-01", billingMonth: "June 2026", billed: false },
  ]).returning();

  // Bills
  const now = new Date();
  const dueDate = new Date(now.getFullYear(), now.getMonth() + 1, 15).toISOString().split("T")[0];

  await db.insert(billsTable).values([
    {
      billNumber: "#000001",
      customerId: customers[0].id,
      meterReadingId: readings[0].id,
      unitsConsumed: "120",
      energyCharges: "300.00",
      fixedCharges: "50.00",
      taxAmount: "63.00",
      totalAmount: "413.00",
      billingMonth: "June 2026",
      dueDate,
      status: "PENDING",
    },
    {
      billNumber: "#000002",
      customerId: customers[1].id,
      meterReadingId: readings[1].id,
      unitsConsumed: "260",
      energyCharges: "890.00",
      fixedCharges: "50.00",
      taxAmount: "170.82",
      totalAmount: "1110.82",
      billingMonth: "June 2026",
      dueDate,
      status: "PAID",
    },
    {
      billNumber: "#000003",
      customerId: customers[2].id,
      meterReadingId: readings[2].id,
      unitsConsumed: "850",
      energyCharges: "5450.00",
      fixedCharges: "200.00",
      taxAmount: "1017.00",
      totalAmount: "6667.00",
      billingMonth: "June 2026",
      dueDate,
      status: "PAID",
    },
    {
      billNumber: "#000004",
      customerId: customers[3].id,
      meterReadingId: readings[3].id,
      unitsConsumed: "2200",
      energyCharges: "11000.00",
      fixedCharges: "500.00",
      taxAmount: "2070.00",
      totalAmount: "13570.00",
      billingMonth: "June 2026",
      dueDate,
      status: "PAID",
    },
    {
      billNumber: "#000005",
      customerId: customers[4].id,
      meterReadingId: readings[4].id,
      unitsConsumed: "80",
      energyCharges: "200.00",
      fixedCharges: "50.00",
      taxAmount: "45.00",
      totalAmount: "295.00",
      billingMonth: "June 2026",
      dueDate,
      status: "OVERDUE",
    },
  ]);

  console.log("Seeding complete.");
  process.exit(0);
}

seed().catch((e) => { console.error(e); process.exit(1); });
