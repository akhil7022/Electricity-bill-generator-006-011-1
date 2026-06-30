import { useState } from "react";

const SIDEBAR_ITEMS = [
  { key: "dashboard",  icon: "📊", label: "Dashboard" },
  { key: "customers",  icon: "👤", label: "Customers" },
  { key: "meter",      icon: "🔌", label: "Meter Readings" },
  { key: "bills",      icon: "📄", label: "Bills" },
  { key: "tariffs",    icon: "⚙️",  label: "Tariffs" },
];

const CUSTOMERS = [
  [1, "Ravi Kumar",        "ravi@example.com",       "9876543210", "MTR-001", "RESIDENTIAL", "01-Jun-2026"],
  [2, "Priya Sharma",      "priya@example.com",      "9876543211", "MTR-002", "COMMERCIAL",  "01-Jun-2026"],
  [3, "Shankar Das",       "shankar@example.com",    "9876543212", "MTR-003", "INDUSTRIAL",  "01-Jun-2026"],
  [4, "Sri Lakshmi Textiles","laxmi@example.com",    "9876543213", "MTR-004", "COMMERCIAL",  "01-Jun-2026"],
  [5, "Anand Raj",         "anand@example.com",      "9876543214", "MTR-005", "RESIDENTIAL", "01-Jun-2026"],
];

const BILLS = [
  [1, "#000001", "Ravi Kumar",           "June 2026", "98.00",  "₹245.00", "₹50.00", "₹53.10", "₹413.00",  "30-Jun-2026", "PENDING"],
  [2, "#000002", "Priya Sharma",         "June 2026", "145.00", "₹580.00", "₹75.00", "₹118.35","₹1,110.82","30-Jun-2026", "PAID"],
  [3, "#000003", "Shankar Das",          "June 2026", "420.00", "₹3,450.00","₹150.00","₹648.00","₹6,667.00","30-Jun-2026", "PAID"],
  [4, "#000004", "Sri Lakshmi Textiles", "June 2026", "980.00", "₹8,500.00","₹200.00","₹1,260.00","₹13,570.00","30-Jun-2026","PAID"],
  [5, "#000005", "Anand Raj",            "June 2026", "55.00",  "₹165.00", "₹50.00", "₹39.15", "₹295.00",  "15-Jun-2026", "OVERDUE"],
];

const METER = [
  [1, "Ravi Kumar",   "MTR-001", "1200", "1298", "98.00",  "2026-06-01", "2026-06", "✅ Yes"],
  [2, "Priya Sharma", "MTR-002", "2300", "2445", "145.00", "2026-06-01", "2026-06", "✅ Yes"],
  [3, "Shankar Das",  "MTR-003", "5100", "5520", "420.00", "2026-06-01", "2026-06", "✅ Yes"],
  [4, "Sri Lakshmi Textiles","MTR-004","8900","9880","980.00","2026-06-01","2026-06","✅ Yes"],
  [5, "Anand Raj",    "MTR-005", "3400", "3455", "55.00",  "2026-06-01", "2026-06", "✅ Yes"],
];

const TARIFFS = [
  [1, "Residential Slab 1", "RESIDENTIAL", 0,   100, "3.0000", "50.00"],
  [2, "Residential Slab 2", "RESIDENTIAL", 101, 200, "5.0000", "50.00"],
  [3, "Residential Slab 3", "RESIDENTIAL", 201, null,"7.0000", "50.00"],
  [4, "Commercial Base",    "COMMERCIAL",  0,   500, "6.5000", "75.00"],
  [5, "Commercial High",    "COMMERCIAL",  501, null,"9.0000", "75.00"],
  [6, "Industrial Rate",    "INDUSTRIAL",  0,   null,"8.5000", "150.00"],
];

function statusBadge(s: string) {
  const colors: Record<string, string> = {
    PAID: "#22c55e", PENDING: "#eab308", OVERDUE: "#ef4444",
  };
  return (
    <span style={{ color: colors[s] ?? "#aaa", fontWeight: 700, fontSize: 12 }}>{s}</span>
  );
}

function Table({ cols, rows, statusCol }: { cols: string[]; rows: any[][]; statusCol?: number }) {
  return (
    <div style={{ overflow: "auto", flex: 1 }}>
      <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}>
        <thead>
          <tr style={{ background: "#1e1b32", borderBottom: "2px solid #3d3861" }}>
            {cols.map((c) => (
              <th key={c} style={{ padding: "10px 12px", textAlign: "left", color: "#a09bc8", fontWeight: 700, whiteSpace: "nowrap" }}>{c}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, ri) => (
            <tr key={ri} style={{ borderBottom: "1px solid #2a2545", background: ri % 2 === 0 ? "transparent" : "#1a1730" }}
              onMouseEnter={(e) => (e.currentTarget.style.background = "#2d2950")}
              onMouseLeave={(e) => (e.currentTarget.style.background = ri % 2 === 0 ? "transparent" : "#1a1730")}
            >
              {row.map((cell, ci) => (
                <td key={ci} style={{ padding: "9px 12px", color: "#e0dcff", whiteSpace: "nowrap" }}>
                  {statusCol !== undefined && ci === statusCol ? statusBadge(String(cell)) : String(cell ?? "∞")}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function StatCard({ label, value, icon, accent }: { label: string; value: string; icon: string; accent: string }) {
  return (
    <div style={{ background: "#28254a", borderRadius: 8, padding: "18px 20px", flex: 1, minWidth: 0 }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
        <span style={{ fontSize: 13, color: "#a09bc8" }}>{label}</span>
        <span style={{ fontSize: 20 }}>{icon}</span>
      </div>
      <div style={{ fontSize: 24, fontWeight: 700, color: "#f0f0ff" }}>{value}</div>
    </div>
  );
}

function ActionBtn({ label, color, onClick }: { label: string; color: string; onClick?: () => void }) {
  return (
    <button onClick={onClick} style={{
      background: color, color: "#fff", border: "none", borderRadius: 6,
      padding: "8px 14px", fontSize: 13, fontWeight: 600, cursor: "pointer",
      marginRight: 8
    }}>{label}</button>
  );
}

function DashboardView() {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      <div style={{ display: "flex", gap: 16 }}>
        <StatCard label="Total Customers"  value="5"          icon="👥" accent="#634bff" />
        <StatCard label="Pending Dues"     value="₹413.00"    icon="⏳" accent="#eab308" />
        <StatCard label="Total Revenue"    value="₹21,347.82" icon="💰" accent="#22c55e" />
        <StatCard label="Bills This Month" value="5"          icon="📄" accent="#8b5cf6" />
      </div>
      <div style={{ background: "#28254a", borderRadius: 8, padding: 20, flex: 1 }}>
        <div style={{ fontSize: 15, fontWeight: 700, color: "#f0f0ff", marginBottom: 16 }}>Recent Bills</div>
        <Table
          cols={["Bill No.", "Customer", "Month", "Amount ₹", "Status"]}
          rows={BILLS.map(b => [b[1], b[2], b[3], b[8], b[10]])}
          statusCol={4}
        />
      </div>
    </div>
  );
}

function CustomersView() {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12, height: "100%" }}>
      <div style={{ display: "flex", gap: 8 }}>
        <ActionBtn label="+ Add"   color="#634bff" />
        <ActionBtn label="✏ Edit"  color="#28254a" />
        <ActionBtn label="🗑 Delete" color="#ef4444" />
      </div>
      <Table cols={["ID","Name","Email","Phone","Meter ID","Type","Registered"]} rows={CUSTOMERS} />
    </div>
  );
}

function MeterView() {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12, height: "100%" }}>
      <div><ActionBtn label="+ Record Reading" color="#634bff" /></div>
      <Table cols={["ID","Customer","Meter ID","Previous","Current","Units","Date","Month","Billed?"]} rows={METER} />
    </div>
  );
}

function BillsView() {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12, height: "100%" }}>
      <div style={{ display: "flex", gap: 8 }}>
        <ActionBtn label="+ Generate Bill" color="#634bff" />
        <ActionBtn label="✔ Mark Paid"    color="#22c55e" />
        <ActionBtn label="⚠ Overdue"      color="#ef4444" />
      </div>
      <Table
        cols={["ID","Bill No.","Customer","Month","Units","Energy ₹","Fixed ₹","Tax ₹","Total ₹","Due Date","Status"]}
        rows={BILLS}
        statusCol={10}
      />
    </div>
  );
}

function TariffsView() {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12, height: "100%" }}>
      <div style={{ display: "flex", gap: 8 }}>
        <ActionBtn label="+ Add Slab" color="#634bff" />
        <ActionBtn label="🗑 Delete"  color="#ef4444" />
      </div>
      <Table
        cols={["ID","Name","Connection Type","Units From","Units To","Rate/Unit (₹)","Fixed Charge (₹)"]}
        rows={TARIFFS.map(r => [r[0],r[1],r[2],r[3],r[4]??'∞',r[5],r[6]])}
      />
    </div>
  );
}

const VIEWS: Record<string, { title: string; sub: string; component: JSX.Element }> = {
  dashboard: { title: "Overview",              sub: "System-wide metrics and recent activity", component: <DashboardView /> },
  customers: { title: "Customers",             sub: "Manage electricity consumers",            component: <CustomersView /> },
  meter:     { title: "Meter Readings",        sub: "Record monthly electricity consumption",  component: <MeterView /> },
  bills:     { title: "Bills",                 sub: "Generate and manage electricity bills",   component: <BillsView /> },
  tariffs:   { title: "Tariff Configuration",  sub: "Configure slab-based electricity pricing rates", component: <TariffsView /> },
};

export default function SwingPreview() {
  const [active, setActive] = useState("dashboard");
  const view = VIEWS[active];

  return (
    <div style={{
      display: "flex", height: "100vh", fontFamily: "'Segoe UI', sans-serif",
      background: "#18162a", overflow: "hidden"
    }}>

      {/* ── Sidebar ── */}
      <div style={{ width: 220, background: "#1e1b32", display: "flex", flexDirection: "column", flexShrink: 0 }}>
        {/* Logo */}
        <div style={{ padding: "22px 20px 16px", display: "flex", alignItems: "center", gap: 10, borderBottom: "1px solid #2d2a50" }}>
          <span style={{ fontSize: 22, color: "#634bff" }}>⚡</span>
          <span style={{ fontSize: 17, fontWeight: 700, color: "#f0f0ff" }}>VoltGrid</span>
        </div>

        {/* Nav */}
        <div style={{ flex: 1, paddingTop: 8 }}>
          {SIDEBAR_ITEMS.map(({ key, icon, label }) => {
            const isActive = active === key;
            return (
              <div key={key} onClick={() => setActive(key)} style={{
                display: "flex", alignItems: "center", gap: 10,
                padding: "11px 20px", cursor: "pointer", userSelect: "none",
                background: isActive ? "#634bff" : "transparent",
                color: isActive ? "#fff" : "#a09bc8",
                fontWeight: isActive ? 700 : 400,
                fontSize: 14,
                borderRadius: isActive ? "0 8px 8px 0" : 0,
                marginRight: isActive ? 8 : 0,
                transition: "all 0.15s",
              }}
                onMouseEnter={(e) => { if (!isActive) e.currentTarget.style.background = "#2a2650"; }}
                onMouseLeave={(e) => { if (!isActive) e.currentTarget.style.background = "transparent"; }}
              >
                <span style={{ fontSize: 16 }}>{icon}</span>
                {label}
              </div>
            );
          })}
        </div>

        {/* Footer */}
        <div style={{ padding: "12px 20px", fontSize: 11, color: "#6b6895", fontStyle: "italic" }}>
          VoltGrid Swing v1.0
        </div>
      </div>

      {/* ── Content ── */}
      <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        {/* Window title bar */}
        <div style={{ background: "#1a1730", borderBottom: "1px solid #2d2a50", padding: "0 20px", display: "flex", alignItems: "center", justifyContent: "space-between", height: 38, flexShrink: 0 }}>
          <span style={{ fontSize: 12, color: "#a09bc8" }}>⚡ VoltGrid Admin — Java Swing Desktop Application</span>
          <div style={{ display: "flex", gap: 8 }}>
            {["#eab308","#22c55e","#ef4444"].map((c,i) => (
              <div key={i} style={{ width: 12, height: 12, borderRadius: "50%", background: c }} />
            ))}
          </div>
        </div>

        {/* Page header */}
        <div style={{ padding: "22px 28px 16px", flexShrink: 0 }}>
          <div style={{ fontSize: 24, fontWeight: 700, color: "#f0f0ff" }}>{view.title}</div>
          <div style={{ fontSize: 13, color: "#a09bc8", marginTop: 4 }}>{view.sub}</div>
        </div>

        {/* Main content */}
        <div style={{ flex: 1, overflow: "auto", padding: "0 28px 28px", display: "flex", flexDirection: "column" }}>
          {view.component}
        </div>
      </div>
    </div>
  );
}
