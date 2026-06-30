package com.voltgrid.swing.panel;

import com.voltgrid.swing.db.DatabaseHelper;
import com.voltgrid.swing.util.TableUtil;
import com.voltgrid.swing.util.UIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Bills panel — lists all bills, lets the user generate a new bill
 * or mark one as PAID / OVERDUE.
 */
public class BillsPanel extends JPanel implements Refreshable {

    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Bill No.", "Customer", "Month", "Units", "Energy ₹", "Fixed ₹", "Tax ₹", "Total ₹", "Due Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public BillsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(24, 22, 42));

        add(header(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(new Color(24, 22, 42));
        body.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        body.add(toolbar(), BorderLayout.NORTH);
        body.add(TableUtil.wrap(table), BorderLayout.CENTER);

        // Status column uses color renderer
        table.getColumnModel().getColumn(10).setCellRenderer(TableUtil.statusRenderer());

        add(body, BorderLayout.CENTER);
        refresh();
    }

    private JPanel header() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 20));
        p.setBackground(new Color(24, 22, 42));
        JLabel t = new JLabel("Bills");
        t.setFont(new Font("Segoe UI", Font.BOLD, 26));
        t.setForeground(UIColors.TEXT_PRIMARY);
        JLabel s = new JLabel("Generate and manage electricity bills");
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setForeground(UIColors.TEXT_SECONDARY);
        JPanel col = new JPanel(); col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(new Color(24, 22, 42)); col.add(t); col.add(Box.createVerticalStrut(4)); col.add(s);
        p.add(col); return p;
    }

    private JPanel toolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(new Color(24, 22, 42));
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JButton gen   = TableUtil.actionButton("+ Generate Bill", UIColors.ACCENT_BLUE);
        JButton pay   = TableUtil.actionButton("✔ Mark Paid",     UIColors.SUCCESS);
        JButton over  = TableUtil.actionButton("⚠ Overdue",       UIColors.DANGER);
        gen.setPreferredSize(new Dimension(150, 36));
        pay.setPreferredSize(new Dimension(130, 36));

        gen.addActionListener(e  -> openGenerateForm());
        pay.addActionListener(e  -> updateStatus("PAID"));
        over.addActionListener(e -> updateStatus("OVERDUE"));

        p.add(gen); p.add(pay); p.add(over);
        return p;
    }

    // ── Generate Bill form ────────────────────────────────────────────────────

    private void openGenerateForm() {
        // Unbilled meter readings
        List<Object[]> readings = DatabaseHelper.getInstance().query(
            "SELECT mr.id, c.name, mr.billing_month, mr.units_consumed, c.connection_type, c.id AS cid " +
            "FROM meter_readings mr JOIN customers c ON c.id=mr.customer_id " +
            "WHERE mr.billed=false ORDER BY mr.id",
            rs -> {
                List<Object[]> list = new ArrayList<>();
                while (rs.next()) list.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("billing_month"),
                    rs.getDouble("units_consumed"),
                    rs.getString("connection_type"),
                    rs.getInt("cid")
                });
                return list;
            });

        if (readings.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No unbilled meter readings found.\nRecord a meter reading first.",
                "Nothing to Bill", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] labels = readings.stream()
            .map(r -> "ID " + r[0] + " – " + r[1] + " | " + r[2] + " | " + r[3] + " units")
            .toArray(String[]::new);

        JComboBox<String> fReading = new JComboBox<>(labels);
        JTextField fDue = new JTextField(LocalDate.now().plusDays(30).toString());

        int result = JOptionPane.showConfirmDialog(this,
            new Object[]{"Unbilled Reading:", fReading, "Due Date (YYYY-MM-DD):", fDue},
            "Generate Bill", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        int  idx       = fReading.getSelectedIndex();
        Object[] rRow  = readings.get(idx);
        int  readingId = (int) rRow[0];
        int  custId    = (int) rRow[5];
        String connType= (String) rRow[4];
        double units   = (double) rRow[3];
        String month   = (String) rRow[2];
        String due     = fDue.getText().trim();

        try {
            LocalDate.parse(due); // validate
            generateBillInDB(custId, readingId, connType, units, month, LocalDate.parse(due));
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Bill Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Pure-Java slab billing logic — mirrors BillingService.java:
     *   1. Fetch tariff slabs for this connection type
     *   2. Apply slabs to units consumed
     *   3. Add fixed charge
     *   4. Apply 18% GST
     */
    private void generateBillInDB(int custId, int readingId, String connType,
                                   double totalUnits, String month, LocalDate dueDate) {
        DatabaseHelper db = DatabaseHelper.getInstance();

        // Fetch slabs ordered by units_from
        List<double[]> slabs = db.query(
            "SELECT units_from, units_to, rate_per_unit, fixed_charge FROM tariffs " +
            "WHERE connection_type=?::connection_type ORDER BY units_from",
            rs -> {
                List<double[]> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new double[]{
                        rs.getDouble("units_from"),
                        rs.getObject("units_to") != null ? rs.getDouble("units_to") : -1,
                        rs.getDouble("rate_per_unit"),
                        rs.getDouble("fixed_charge")
                    });
                }
                return list;
            }, connType);

        BigDecimal units   = BigDecimal.valueOf(totalUnits);
        BigDecimal energy  = BigDecimal.ZERO;
        BigDecimal fixed   = BigDecimal.ZERO;

        if (slabs.isEmpty()) {
            energy = units.multiply(new BigDecimal("5.00"));
            fixed  = new BigDecimal("50.00");
        } else {
            BigDecimal remaining = units;
            for (double[] slab : slabs) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal slabFrom = BigDecimal.valueOf(slab[0]);
                BigDecimal slabMax  = slab[1] < 0
                    ? remaining
                    : BigDecimal.valueOf(slab[1]).subtract(slabFrom);
                BigDecimal slabUnits = remaining.min(slabMax);
                energy    = energy.add(slabUnits.multiply(BigDecimal.valueOf(slab[2])));
                remaining = remaining.subtract(slabUnits);
            }
            fixed = BigDecimal.valueOf(slabs.get(0)[3]);
        }

        BigDecimal taxBase = energy.add(fixed);
        BigDecimal tax     = taxBase.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total   = taxBase.add(tax).setScale(2, RoundingMode.HALF_UP);
        energy  = energy.setScale(2, RoundingMode.HALF_UP);
        fixed   = fixed.setScale(2, RoundingMode.HALF_UP);

        long count = db.query("SELECT COUNT(*) FROM bills", rs -> { rs.next(); return rs.getLong(1); });
        String billNo = String.format("#%06d", count + 1);

        db.update(
            "INSERT INTO bills (bill_number,customer_id,meter_reading_id,units_consumed," +
            "energy_charges,fixed_charges,tax_amount,total_amount,billing_month,due_date,status) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?::date,'PENDING')",
            billNo, custId, readingId, units, energy, fixed, tax, total, month, dueDate.toString());

        db.update("UPDATE meter_readings SET billed=true WHERE id=?", readingId);

        JOptionPane.showMessageDialog(this,
            "Bill " + billNo + " generated!\n" +
            "Energy: ₹" + energy + " | Fixed: ₹" + fixed + " | GST: ₹" + tax + "\nTotal: ₹" + total,
            "Bill Generated", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Status update ─────────────────────────────────────────────────────────

    private void updateStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a bill first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        DatabaseHelper.getInstance().update("UPDATE bills SET status=?::bill_status WHERE id=?", status, id);
        refresh();
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    @Override
    public void refresh() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                return DatabaseHelper.getInstance().query(
                    "SELECT b.id, b.bill_number, c.name, b.billing_month, b.units_consumed, " +
                    "b.energy_charges, b.fixed_charges, b.tax_amount, b.total_amount, b.due_date, b.status " +
                    "FROM bills b JOIN customers c ON c.id=b.customer_id ORDER BY b.id DESC",
                    rs -> {
                        List<Object[]> rows = new ArrayList<>();
                        while (rs.next()) rows.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("bill_number"),
                            rs.getString("name"),
                            rs.getString("billing_month"),
                            rs.getString("units_consumed"),
                            "₹" + rs.getString("energy_charges"),
                            "₹" + rs.getString("fixed_charges"),
                            "₹" + rs.getString("tax_amount"),
                            "₹" + rs.getString("total_amount"),
                            rs.getString("due_date"),
                            rs.getString("status")
                        });
                        return rows;
                    });
            }
            @Override
            protected void done() {
                try {
                    model.setRowCount(0);
                    for (Object[] row : get()) model.addRow(row);
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}
