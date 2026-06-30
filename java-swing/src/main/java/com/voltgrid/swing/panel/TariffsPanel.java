package com.voltgrid.swing.panel;

import com.voltgrid.swing.db.DatabaseHelper;
import com.voltgrid.swing.util.TableUtil;
import com.voltgrid.swing.util.UIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tariff Slabs panel — CRUD for electricity pricing slabs.
 */
public class TariffsPanel extends JPanel implements Refreshable {

    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Name", "Connection Type", "Units From", "Units To", "Rate/Unit (₹)", "Fixed Charge (₹)"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public TariffsPanel() {
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
        JLabel t = new JLabel("Tariff Configuration");
        t.setFont(new Font("Segoe UI", Font.BOLD, 26));
        t.setForeground(UIColors.TEXT_PRIMARY);
        JLabel s = new JLabel("Configure slab-based electricity pricing rates");
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

        JButton add = TableUtil.actionButton("+ Add Slab",   UIColors.ACCENT_BLUE);
        JButton del = TableUtil.actionButton("🗑 Delete",     UIColors.DANGER);

        add.addActionListener(e -> openForm());
        del.addActionListener(e -> deleteSelected());

        p.add(add); p.add(del);
        return p;
    }

    private void openForm() {
        JTextField fName   = new JTextField();
        JComboBox<String> fType = new JComboBox<>(new String[]{"RESIDENTIAL","COMMERCIAL","INDUSTRIAL"});
        JTextField fFrom   = new JTextField("0");
        JTextField fTo     = new JTextField("(leave blank for unlimited)");
        JTextField fRate   = new JTextField();
        JTextField fFixed  = new JTextField("50");

        Object[] fields = {
            "Slab Name:",        fName,
            "Connection Type:",  fType,
            "Units From:",       fFrom,
            "Units To (blank=∞):", fTo,
            "Rate per Unit (₹):", fRate,
            "Fixed Charge (₹):", fFixed
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Add Tariff Slab",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String name   = fName.getText().trim();
            String type   = (String) fType.getSelectedItem();
            int    from   = Integer.parseInt(fFrom.getText().trim());
            String toText = fTo.getText().trim();
            Integer to    = (toText.isEmpty() || toText.startsWith("(")) ? null : Integer.parseInt(toText);
            double rate   = Double.parseDouble(fRate.getText().trim());
            double fixed  = Double.parseDouble(fFixed.getText().trim());

            if (name.isEmpty() || rate <= 0) {
                JOptionPane.showMessageDialog(this, "Name and a positive rate are required.",
                    "Validation", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (to != null) {
                DatabaseHelper.getInstance().update(
                    "INSERT INTO tariffs (name,units_from,units_to,rate_per_unit,fixed_charge,connection_type) " +
                    "VALUES (?,?,?,?,?,?::connection_type)", name, from, to, rate, fixed, type);
            } else {
                DatabaseHelper.getInstance().update(
                    "INSERT INTO tariffs (name,units_from,rate_per_unit,fixed_charge,connection_type) " +
                    "VALUES (?,?,?,?,?::connection_type)", name, from, rate, fixed, type);
            }
            refresh();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Units and rates must be valid numbers.",
                "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this, "Delete this tariff slab?",
            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        DatabaseHelper.getInstance().update("DELETE FROM tariffs WHERE id=?", id);
        refresh();
    }

    @Override
    public void refresh() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                return DatabaseHelper.getInstance().query(
                    "SELECT id,name,connection_type,units_from,units_to,rate_per_unit,fixed_charge " +
                    "FROM tariffs ORDER BY connection_type, units_from",
                    rs -> {
                        List<Object[]> rows = new ArrayList<>();
                        while (rs.next()) {
                            Object to = rs.getObject("units_to");
                            rows.add(new Object[]{
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("connection_type"),
                                rs.getInt("units_from"),
                                to != null ? to.toString() : "∞",
                                rs.getString("rate_per_unit"),
                                rs.getString("fixed_charge")
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
