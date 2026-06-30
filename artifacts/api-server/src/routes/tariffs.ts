import { Router } from "express";
import { db } from "@workspace/db";
import { tariffsTable, insertTariffSchema } from "@workspace/db/schema";
import { eq } from "drizzle-orm";

const router = Router();

router.get("/tariffs", async (req, res) => {
  try {
    const tariffs = await db.select().from(tariffsTable).orderBy(tariffsTable.id);
    res.json(tariffs);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to fetch tariffs" });
  }
});

router.post("/tariffs", async (req, res) => {
  try {
    const parsed = insertTariffSchema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: parsed.error.issues });
    const [tariff] = await db.insert(tariffsTable).values(parsed.data).returning();
    res.status(201).json(tariff);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to create tariff" });
  }
});

router.put("/tariffs/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    const parsed = insertTariffSchema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: parsed.error.issues });
    const [tariff] = await db.update(tariffsTable).set(parsed.data).where(eq(tariffsTable.id, id)).returning();
    if (!tariff) return res.status(404).json({ error: "Tariff not found" });
    res.json(tariff);
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to update tariff" });
  }
});

router.delete("/tariffs/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id);
    await db.delete(tariffsTable).where(eq(tariffsTable.id, id));
    res.status(204).send();
  } catch (err) {
    req.log.error(err);
    res.status(500).json({ error: "Failed to delete tariff" });
  }
});

export default router;
