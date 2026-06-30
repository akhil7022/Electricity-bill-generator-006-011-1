import { pgTable, serial, integer, numeric, date, text, boolean, timestamp } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";
import { customersTable } from "./customers";

export const meterReadingsTable = pgTable("meter_readings", {
  id: serial("id").primaryKey(),
  customerId: integer("customer_id").notNull().references(() => customersTable.id),
  previousReading: numeric("previous_reading", { precision: 10, scale: 2 }).notNull(),
  currentReading: numeric("current_reading", { precision: 10, scale: 2 }).notNull(),
  unitsConsumed: numeric("units_consumed", { precision: 10, scale: 2 }).notNull(),
  readingDate: date("reading_date").notNull(),
  billingMonth: text("billing_month").notNull(),
  billed: boolean("billed").default(false).notNull(),
  createdAt: timestamp("created_at").defaultNow().notNull(),
});

export const insertMeterReadingSchema = createInsertSchema(meterReadingsTable).omit({ id: true, createdAt: true, unitsConsumed: true, billed: true });
export type InsertMeterReading = z.infer<typeof insertMeterReadingSchema>;
export type MeterReading = typeof meterReadingsTable.$inferSelect;
