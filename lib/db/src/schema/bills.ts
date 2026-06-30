import { pgTable, serial, text, integer, numeric, date, timestamp, pgEnum } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";
import { customersTable } from "./customers";
import { meterReadingsTable } from "./meter-readings";

export const billStatusEnum = pgEnum("bill_status", ["PENDING", "PAID", "OVERDUE"]);

export const billsTable = pgTable("bills", {
  id: serial("id").primaryKey(),
  billNumber: text("bill_number").notNull().unique(),
  customerId: integer("customer_id").notNull().references(() => customersTable.id),
  meterReadingId: integer("meter_reading_id").notNull().references(() => meterReadingsTable.id),
  unitsConsumed: numeric("units_consumed", { precision: 10, scale: 2 }).notNull(),
  energyCharges: numeric("energy_charges", { precision: 10, scale: 2 }).notNull(),
  fixedCharges: numeric("fixed_charges", { precision: 10, scale: 2 }).notNull(),
  taxAmount: numeric("tax_amount", { precision: 10, scale: 2 }).notNull(),
  totalAmount: numeric("total_amount", { precision: 10, scale: 2 }).notNull(),
  billingMonth: text("billing_month").notNull(),
  dueDate: date("due_date").notNull(),
  status: billStatusEnum("status").default("PENDING").notNull(),
  createdAt: timestamp("created_at").defaultNow().notNull(),
});

export const insertBillSchema = createInsertSchema(billsTable).omit({ id: true, billNumber: true, createdAt: true });
export type InsertBill = z.infer<typeof insertBillSchema>;
export type Bill = typeof billsTable.$inferSelect;
