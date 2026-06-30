import { Router } from "express";
import { db } from "@workspace/db";
import { customersTable, insertCustomerSchema } from "@workspace/db/schema";
import { eq } from "drizzle-orm";

const router = Router();

router.get("/customers", async (req, res) => {
  try {
    const customers = await db.select().from(customersTable).orderBy(customersTable.id);
    res.json(customers);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to fetch customers" });
  }
});

router.get("/customers/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    const [customer] = await db.select().from(customersTable).where(eq(customersTable.id, id));
    if (!customer) return res.status(404).json({ error: "Customer not found" });
    res.json(customer);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to fetch customer" });
  }
});

router.post("/customers", async (req, res) => {
  try {
    const parsed = insertCustomerSchema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: parsed.error.issues });
    const [customer] = await db.insert(customersTable).values(parsed.data).returning();
    res.status(201).json(customer);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to create customer" });
  }
});

router.put("/customers/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    const parsed = insertCustomerSchema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: parsed.error.issues });
    const [customer] = await db.update(customersTable).set(parsed.data).where(eq(customersTable.id, id)).returning();
    if (!customer) return res.status(404).json({ error: "Customer not found" });
    res.json(customer);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to update customer" });
  }
});

router.delete("/customers/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    await db.delete(customersTable).where(eq(customersTable.id, id));
    res.status(204).send();
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to delete customer" });
  }
});

export default router;
