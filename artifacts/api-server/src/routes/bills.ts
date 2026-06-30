import { Router } from "express";
import { db } from "@workspace/db";
import { billsTable, customersTable, meterReadingsTable, tariffsTable } from "@workspace/db/schema";
import { eq, and, sql, sum, count } from "drizzle-orm";

const router = Router();

router.get("/bills", async (req, res) => {
  try {
    const customerId = req.query.customerId ? parseInt(req.query.customerId as string) : undefined;
    const status = req.query.status as string | undefined;

    const conditions = [];
    if (customerId) conditions.push(eq(billsTable.customerId, customerId));
    if (status) conditions.push(eq(billsTable.status, status as "PENDING" | "PAID" | "OVERDUE"));

    const rows = await db
      .select({
        id: billsTable.id,
        billNumber: billsTable.billNumber,
        customerId: billsTable.customerId,
        customerName: customersTable.name,
        meterId: customersTable.meterId,
        meterReadingId: billsTable.meterReadingId,
        unitsConsumed: billsTable.unitsConsumed,
        energyCharges: billsTable.energyCharges,
        fixedCharges: billsTable.fixedCharges,
        taxAmount: billsTable.taxAmount,
        totalAmount: billsTable.totalAmount,
        billingMonth: billsTable.billingMonth,
        dueDate: billsTable.dueDate,
        status: billsTable.status,
        createdAt: billsTable.createdAt,
      })
      .from(billsTable)
      .innerJoin(customersTable, eq(billsTable.customerId, customersTable.id))
      .where(conditions.length > 0 ? and(...conditions) : sql`1=1`)
      .orderBy(billsTable.id);

    res.json(rows);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to fetch bills" });
  }
});

router.get("/bills/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    const [row] = await db
      .select({
        id: billsTable.id,
        billNumber: billsTable.billNumber,
        customerId: billsTable.customerId,
        customerName: customersTable.name,
        meterId: customersTable.meterId,
        meterReadingId: billsTable.meterReadingId,
        unitsConsumed: billsTable.unitsConsumed,
        energyCharges: billsTable.energyCharges,
        fixedCharges: billsTable.fixedCharges,
        taxAmount: billsTable.taxAmount,
        totalAmount: billsTable.totalAmount,
        billingMonth: billsTable.billingMonth,
        dueDate: billsTable.dueDate,
        status: billsTable.status,
        createdAt: billsTable.createdAt,
      })
      .from(billsTable)
      .innerJoin(customersTable, eq(billsTable.customerId, customersTable.id))
      .where(eq(billsTable.id, id));
    if (!row) return res.status(404).json({ error: "Bill not found" });
    res.json(row);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to fetch bill" });
  }
});

router.post("/bills", async (req, res) => {
  try {
    const { customerId, meterReadingId, dueDate } = req.body;
    if (!customerId || !meterReadingId || !dueDate) {
      return res.status(400).json({ error: "customerId, meterReadingId, and dueDate are required" });
    }

    const [customer] = await db.select().from(customersTable).where(eq(customersTable.id, customerId));
    if (!customer) return res.status(404).json({ error: "Customer not found" });

    const [reading] = await db.select().from(meterReadingsTable).where(eq(meterReadingsTable.id, meterReadingId));
    if (!reading) return res.status(404).json({ error: "Meter reading not found" });

    const units = parseFloat(String(reading.unitsConsumed));

    const tariffs = await db
      .select()
      .from(tariffsTable)
      .where(eq(tariffsTable.connectionType, customer.connectionType))
      .orderBy(tariffsTable.unitsFrom);

    let energyCharges = 0;
    let fixedCharges = 0;

    if (tariffs.length === 0) {
      energyCharges = units * 5;
      fixedCharges = 50;
    } else {
      let remainingUnits = units;
      for (const slab of tariffs) {
        if (remainingUnits <= 0) break;
        const slabMax = slab.unitsTo !== null ? slab.unitsTo - slab.unitsFrom : Infinity;
        const slabUnits = Math.min(remainingUnits, slabMax);
        energyCharges += slabUnits * parseFloat(String(slab.ratePerUnit));
        remainingUnits -= slabUnits;
      }
      fixedCharges = parseFloat(String(tariffs[0].fixedCharge));
    }

    const taxAmount = (energyCharges + fixedCharges) * 0.18;
    const totalAmount = energyCharges + fixedCharges + taxAmount;

    const [countRow] = await db.select({ cnt: count() }).from(billsTable);
    const billNumber = `#${String((countRow?.cnt ?? 0) + 1).padStart(6, "0")}`;

    const [bill] = await db
      .insert(billsTable)
      .values({
        billNumber,
        customerId,
        meterReadingId,
        unitsConsumed: String(units),
        energyCharges: energyCharges.toFixed(2),
        fixedCharges: fixedCharges.toFixed(2),
        taxAmount: taxAmount.toFixed(2),
        totalAmount: totalAmount.toFixed(2),
        billingMonth: reading.billingMonth,
        dueDate,
        status: "PENDING",
      })
      .returning();

    await db.update(meterReadingsTable).set({ billed: true }).where(eq(meterReadingsTable.id, meterReadingId));

    res.status(201).json({
      ...bill,
      customerName: customer.name,
      meterId: customer.meterId,
    });
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to generate bill" });
  }
});

router.patch("/bills/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    const { status } = req.body;
    if (!["PENDING", "PAID", "OVERDUE"].includes(status)) {
      return res.status(400).json({ error: "Invalid status" });
    }
    const [bill] = await db
      .update(billsTable)
      .set({ status })
      .where(eq(billsTable.id, id))
      .returning();
    if (!bill) return res.status(404).json({ error: "Bill not found" });

    const [customer] = await db.select().from(customersTable).where(eq(customersTable.id, bill.customerId));
    res.json({ ...bill, customerName: customer?.name ?? "", meterId: customer?.meterId ?? "" });
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to update bill status" });
  }
});

export default router;
