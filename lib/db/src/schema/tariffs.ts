import { pgTable, serial, text, integer, numeric, pgEnum } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";

export const tariffConnectionTypeEnum = pgEnum("tariff_connection_type", ["RESIDENTIAL", "COMMERCIAL", "INDUSTRIAL"]);

export const tariffsTable = pgTable("tariffs", {
  id: serial("id").primaryKey(),
  name: text("name").notNull(),
  unitsFrom: integer("units_from").notNull(),
  unitsTo: integer("units_to"),
  ratePerUnit: numeric("rate_per_unit", { precision: 10, scale: 4 }).notNull(),
  fixedCharge: numeric("fixed_charge", { precision: 10, scale: 2 }).notNull(),
  connectionType: tariffConnectionTypeEnum("connection_type").notNull(),
});

export const insertTariffSchema = createInsertSchema(tariffsTable).omit({ id: true });
export type InsertTariff = z.infer<typeof insertTariffSchema>;
export type Tariff = typeof tariffsTable.$inferSelect;
