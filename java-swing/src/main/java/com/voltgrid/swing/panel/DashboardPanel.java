package com.voltgrid.swing.panel;

import com.voltgrid.swing.db.DatabaseHelper;
import com.voltgrid.swing.util.UIColors;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard panel showing 4 summary stat cards + a recent-bills table.
 */
public class DashboardPanel extends JPanel implements Refreshable {

    private final JLabel lblCustomers   = statValue("—");
    private final JLabel lblPending     = statValue("—");
    private final JLabel lblRevenue     = statValue("—");
    private final JLabel lblBillsMonth  = statValue("—");

    private final DefaultTableModel recentModel = new DefaultTableModel(
        new String[]{"Bill No.", "Customer", "Month", "Amount (₹)", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable recentTable = new JTable(recentModel);

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(24, 22, 42));

        add(header(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 24));
        body.setBackground(new Color(24, 22, 42));
        body.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));
        body.add(buildCards(), BorderLayout.NORTH);
        body.add(buildRecentTable(), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        refresh();
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private JPanel header() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 20));
        p.setBackground(new Color(24, 22, 42));
        JLabel title = new JLabel("Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UIColors.TEXT_PRIMARY);
        JLabel sub = new JLabel("System-wide metrics and recent activity");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UIColors.TEXT_SECONDARY);
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(new Color(24, 22, 42));
        col.add(title);
        col.add(Box.createVerticalStrut(4));
        col.add(sub);
        p.add(col);
        return p;
    }

    // ── Stat cards ───────────────────────────────────────────────────────────

    private JPanel buildCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setBackground(new Color(24, 22, 42));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        row.add(card("Total Customers", lblCustomers, "👥", UIColors.ACCENT_BLUE));
        row.add(card("Pending Dues",    lblPending,   "⏳", UIColors.WARNING));
        row.add(card("Total Revenue",   lblRevenue,   "💰", UIColors.SUCCESS));
        row.add(card("Bills This Month",lblBillsMonth,"📄", new Color(139, 92, 246)));

        return row;
    }

    private JPanel card(String labelText, JLabel valueLabel, String icon, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UIColors.CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setPreferredSize(new Dimension(0, 100));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIColors.CARD_BG);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(UIColors.TEXT_SECONDARY);
        top.add(lbl, BorderLayout.WEST);

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        ico.setForeground(accent);
        top.add(ico, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(UIColors.TEXT_PRIMARY);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private static JLabel statValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 24));
        l.setForeground(UIColors.TEXT_PRIMARY);
        return l;
    }

    // ── Recent bills table ────────────────────────────────────────────────────

    private JPanel buildRecentTable() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(new Color(24, 22, 42));

        JLabel title = new JLabel("Recent Bills");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(UIColors.TEXT_PRIMARY);
        p.add(title, BorderLayout.NORTH);

        recentTable.setRowHeight(38);
        recentTable.setShowGrid(false);
        recentTable.setIntercellSpacing(new Dimension(0, 0));
        recentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        recentTable.getTableHeader().setReorderingAllowed(false);
        recentTable.setFillsViewportHeight(true);
        recentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Status column renderer
        recentTable.getColumnModel().getColumn(4).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    String val = v == null ? "" : v.toString();
                    setForeground(UIColors.statusColor(val));
                    setFont(getFont().deriveFont(Font.BOLD));
                    setHorizontalAlignment(CENTER);
                    return this;
                }
            });

        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIColors.SIDEBAR_HOVER, 1));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    @Override
    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            long   customers, billsMonth;
            double pending, revenue;
            final List<Object[]> rows = new ArrayList<>();

            @Override
            protected Void doInBackground() {
                DatabaseHelper db = DatabaseHelper.getInstance();

                customers  = db.query("SELECT COUNT(*) FROM customers",
                    rs -> { rs.next(); return rs.getLong(1); });
                pending    = db.query(
                    "SELECT COALESCE(SUM(total_amount),0) FROM bills WHERE status='PENDING'",
                    rs -> { rs.next(); return rs.getDouble(1); });
                revenue    = db.query(
                    "SELECT COALESCE(SUM(total_amount),0) FROM bills WHERE status='PAID'",
                    rs -> { rs.next(); return rs.getDouble(1); });
                billsMonth = db.query(
                    "SELECT COUNT(*) FROM bills WHERE TO_CHAR(created_at,'YYYY-MM')=TO_CHAR(NOW(),'YYYY-MM')",
                    rs -> { rs.next(); return rs.getLong(1); });

                db.query(
                    "SELECT b.bill_number, c.name, b.billing_month, b.total_amount, b.status " +
                    "FROM bills b JOIN customers c ON c.id=b.customer_id " +
                    "ORDER BY b.created_at DESC LIMIT 10",
                    rs -> {
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getString("bill_number"),
                                rs.getString("name"),
                                rs.getString("billing_month"),
                                String.format("₹%.2f", rs.getDouble("total_amount")),
                                rs.getString("status")
                            });
                        }
                        return null;
                    });
                return null;
            }

            @Override
            protected void done() {
                lblCustomers.setText(String.valueOf(customers));
                lblPending.setText(String.format("₹%.2f", pending));
                lblRevenue.setText(String.format("₹%.2f", revenue));
                lblBillsMonth.setText(String.valueOf(billsMonth));

                recentModel.setRowCount(0);
                for (Object[] row : rows) recentModel.addRow(row);
            }
        };
        worker.execute();
    }
}
