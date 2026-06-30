package com.voltgrid.swing.panel;

import com.voltgrid.swing.db.DatabaseHelper;
import com.voltgrid.swing.util.TableUtil;
import com.voltgrid.swing.util.UIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Meter Readings panel — shows all readings; allows adding new ones.
 */
public class MeterReadingsPanel extends JPanel implements Refreshable {

    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Customer", "Meter ID", "Previous", "Current", "Units", "Date", "Month", "Billed?"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public MeterReadingsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(24, 22, 42));

        add(header(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(new Color(24, 22, 42));
        body.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        body.add(toolbar(), BorderLayout.NORTH);
        body.add(TableUtil.wrap(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        refresh();
    }

    private JPanel header() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 20));
        p.setBackground(new Color(24, 22, 42));
        JLabel t = new JLabel("Meter Readings");
        t.setFont(new Font("Segoe UI", Font.BOLD, 26));
        t.setForeground(UIColors.TEXT_PRIMARY);
        JLabel s = new JLabel("Record monthly electricity consumption");
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

        JButton add = TableUtil.actionButton("+ Record Reading", UIColors.ACCENT_BLUE);
        add.setPreferredSize(new Dimension(160, 36));
        add.addActionListener(e -> openForm());

        p.add(add);
        return p;
    }

    private void openForm() {
        // Build customer dropdown from DB
        List<Object[]> customers = DatabaseHelper.getInstance().query(
            "SELECT id, name, meter_id FROM customers ORDER BY name",
            rs -> {
                List<Object[]> list = new ArrayList<>();
                while (rs.next())
                    list.add(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("meter_id")});
                return list;
            });

        if (customers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No customers found. Add a customer first.",
                "No Customers", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] customerLabels = customers.stream()
            .map(r -> r[0] + " – " + r[1] + " (" + r[2] + ")")
            .toArray(String[]::new);
        JComboBox<String> fCustomer = new JComboBox<>(customerLabels);
        JTextField fPrev    = new JTextField("0");
        JTextField fCurrent = new JTextField();
        JTextField fDate    = new JTextField(LocalDate.now().toString());
        JTextField fMonth   = new JTextField(LocalDate.now().toString().substring(0, 7));

        Object[] fields = {
            "Customer:",          fCustomer,
            "Previous Reading:",  fPrev,
            "Current Reading:",   fCurrent,
            "Reading Date (YYYY-MM-DD):", fDate,
            "Billing Month (YYYY-MM):",   fMonth
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Record Meter Reading",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            int    idx      = fCustomer.getSelectedIndex();
            int    custId   = (int) customers.get(idx)[0];
            double prev     = Double.parseDouble(fPrev.getText().trim());
            double current  = Double.parseDouble(fCurrent.getText().trim());
            String date     = fDate.getText().trim();
            String month    = fMonth.getText().trim();

            if (current < prev) {
                JOptionPane.showMessageDialog(this,
                    "Current reading must be ≥ previous reading.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double units = current - prev;
            DatabaseHelper.getInstance().update(
                "INSERT INTO meter_readings " +
                "(customer_id, previous_reading, current_reading, units_consumed, reading_date, billing_month, billed) " +
                "VALUES (?, ?, ?, ?, ?::date, ?, false)",
                custId, prev, current, units, date, month);
            refresh();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Readings must be valid numbers.",
                "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                return DatabaseHelper.getInstance().query(
                    "SELECT mr.id, c.name, c.meter_id, mr.previous_reading, mr.current_reading, " +
                    "mr.units_consumed, mr.reading_date, mr.billing_month, mr.billed " +
                    "FROM meter_readings mr JOIN customers c ON c.id=mr.customer_id ORDER BY mr.id DESC",
                    rs -> {
                        List<Object[]> rows = new ArrayList<>();
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("meter_id"),
                                rs.getString("previous_reading"),
                                rs.getString("current_reading"),
                                rs.getString("units_consumed"),
                                rs.getString("reading_date"),
                                rs.getString("billing_month"),
                                rs.getBoolean("billed") ? "✅ Yes" : "❌ No"
                            });
                        }
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
