import { Router, type IRouter } from "express";
import healthRouter from "./health";
import customersRouter from "./customers";
import tariffsRouter from "./tariffs";
import meterReadingsRouter from "./meter-readings";
import billsRouter from "./bills";
import dashboardRouter from "./dashboard";

const router: IRouter = Router();

router.use(healthRouter);
router.use(customersRouter);
router.use(tariffsRouter);
router.use(meterReadingsRouter);
router.use(billsRouter);
router.use(dashboardRouter);

export default router;
