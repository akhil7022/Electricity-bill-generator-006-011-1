import { Router } from "express";
import { db } from "@workspace/db";
import { meterReadingsTable, customersTable, insertMeterReadingSchema } from "@workspace/db/schema";
import { eq, sql } from "drizzle-orm";

const router = Router();

router.get("/meter-readings", async (req, res) => {
  try {
    const customerId = req.query.customerId ? parseInt(req.query.customerId as string) : undefined;
    const rows = await db
      .select({
        id: meterReadingsTable.id,
        customerId: meterReadingsTable.customerId,
        customerName: customersTable.name,
        meterId: customersTable.meterId,
        previousReading: meterReadingsTable.previousReading,
        currentReading: meterReadingsTable.currentReading,
        unitsConsumed: meterReadingsTable.unitsConsumed,
        readingDate: meterReadingsTable.readingDate,
        billingMonth: meterReadingsTable.billingMonth,
        billed: meterReadingsTable.billed,
      })
      .from(meterReadingsTable)
      .innerJoin(customersTable, eq(meterReadingsTable.customerId, customersTable.id))
      .where(customerId ? eq(meterReadingsTable.customerId, customerId) : sql`1=1`)
      .orderBy(meterReadingsTable.id);
    res.json(rows);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to fetch meter readings" });
  }
});

router.get("/meter-readings/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    const [row] = await db
      .select({
        id: meterReadingsTable.id,
        customerId: meterReadingsTable.customerId,
        customerName: customersTable.name,
        meterId: customersTable.meterId,
        previousReading: meterReadingsTable.previousReading,
        currentReading: meterReadingsTable.currentReading,
        unitsConsumed: meterReadingsTable.unitsConsumed,
        readingDate: meterReadingsTable.readingDate,
        billingMonth: meterReadingsTable.billingMonth,
        billed: meterReadingsTable.billed,
      })
      .from(meterReadingsTable)
      .innerJoin(customersTable, eq(meterReadingsTable.customerId, customersTable.id))
      .where(eq(meterReadingsTable.id, id));
    if (!row) return res.status(404).json({ error: "Meter reading not found" });
    res.json(row);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to fetch meter reading" });
  }
});

router.post("/meter-readings", async (req, res) => {
  try {
    const body = req.body;
    const parsed = insertMeterReadingSchema.safeParse(body);
    if (!parsed.success) return res.status(400).json({ error: parsed.error.issues });

    const { previousReading, currentReading } = parsed.data;
    const unitsConsumed = (parseFloat(String(currentReading)) - parseFloat(String(previousReading))).toFixed(2);

    const [reading] = await db
      .insert(meterReadingsTable)
      .values({ ...parsed.data, unitsConsumed })
      .returning();

    const [customer] = await db.select().from(customersTable).where(eq(customersTable.id, reading.customerId));
    res.status(201).json({
      ...reading,
      customerName: customer?.name ?? "",
      meterId: customer?.meterId ?? "",
    });
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to record meter reading" });
  }
});

export default router;
