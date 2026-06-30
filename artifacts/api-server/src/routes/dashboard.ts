import { Router } from "express";
import { db } from "@workspace/db";
import { billsTable, customersTable } from "@workspace/db/schema";
import { eq, sql, count, sum } from "drizzle-orm";

const router = Router();

router.get("/dashboard/summary", async (req, res) => {
  try {
    const [customerCount] = await db.select({ cnt: count() }).from(customersTable);

    const [pendingDues] = await db
      .select({ total: sum(billsTable.totalAmount) })
      .from(billsTable)
      .where(eq(billsTable.status, "PENDING"));

    const [totalRevenue] = await db
      .select({ total: sum(billsTable.totalAmount) })
      .from(billsTable)
      .where(eq(billsTable.status, "PAID"));

    const now = new Date();
    const monthStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
    const [billsThisMonth] = await db
      .select({ cnt: count() })
      .from(billsTable)
      .where(sql`to_char(${billsTable.createdAt}, 'YYYY-MM') = ${monthStr}`);

    const recentBills = await db
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
      .orderBy(sql`${billsTable.createdAt} desc`)
      .limit(5);

    res.json({
      totalCustomers: customerCount?.cnt ?? 0,
      pendingDues: parseFloat(String(pendingDues?.total ?? 0)),
      totalRevenue: parseFloat(String(totalRevenue?.total ?? 0)),
      billsThisMonth: billsThisMonth?.cnt ?? 0,
      recentBills,
    });
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to fetch dashboard summary" });
  }
});

export default router;
