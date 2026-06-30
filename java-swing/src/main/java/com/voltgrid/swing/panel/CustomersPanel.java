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
 * Customers panel — table with Add / Edit / Delete actions.
 */
public class CustomersPanel extends JPanel implements Refreshable {

    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Name", "Email", "Phone", "Meter ID", "Type", "Registered"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public CustomersPanel() {
        setLayout(new BorderLayout(0, 0));
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
        JLabel t = new JLabel("Customers");
        t.setFont(new Font("Segoe UI", Font.BOLD, 26));
        t.setForeground(UIColors.TEXT_PRIMARY);
        JLabel s = new JLabel("Manage electricity consumers");
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

        JButton add  = TableUtil.actionButton("+ Add",   UIColors.ACCENT_BLUE);
        JButton edit = TableUtil.actionButton("✏ Edit",  UIColors.CARD_BG);
        JButton del  = TableUtil.actionButton("🗑 Delete", UIColors.DANGER);

        add.addActionListener(e  -> openForm(null));
        edit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showNoSelection(); return; }
            openForm(row);
        });
        del.addActionListener(e  -> deleteSelected());

        p.add(add); p.add(edit); p.add(del);
        return p;
    }

    // ── CRUD operations ───────────────────────────────────────────────────────

    private void openForm(Integer editRow) {
        JTextField fName   = new JTextField(editRow != null ? (String) model.getValueAt(editRow, 1) : "");
        JTextField fEmail  = new JTextField(editRow != null ? (String) model.getValueAt(editRow, 2) : "");
        JTextField fPhone  = new JTextField(editRow != null ? (String) model.getValueAt(editRow, 3) : "");
        JTextField fMeter  = new JTextField(editRow != null ? (String) model.getValueAt(editRow, 4) : "");
        JComboBox<String> fType = new JComboBox<>(new String[]{"RESIDENTIAL","COMMERCIAL","INDUSTRIAL"});
        if (editRow != null) fType.setSelectedItem(model.getValueAt(editRow, 5));

        Object[] fields = {
            "Name:",  fName,
            "Email:", fEmail,
            "Phone:", fPhone,
            "Meter ID:", fMeter,
            "Connection Type:", fType
        };

        String title  = editRow == null ? "Add Customer" : "Edit Customer";
        int    result = JOptionPane.showConfirmDialog(this, fields, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String name  = fName.getText().trim();
        String email = fEmail.getText().trim();
        String phone = fPhone.getText().trim();
        String meter = fMeter.getText().trim();
        String type  = (String) fType.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || meter.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, email, and meter ID are required.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DatabaseHelper db = DatabaseHelper.getInstance();
        try {
            if (editRow == null) {
                db.update(
                    "INSERT INTO customers (name,email,phone,address,meter_id,connection_type) " +
                    "VALUES (?,?,?,?,?,?::connection_type)",
                    name, email, phone, "", meter, type);
            } else {
                int id = (int) model.getValueAt(editRow, 0);
                db.update(
                    "UPDATE customers SET name=?,email=?,phone=?,meter_id=?,connection_type=?::connection_type WHERE id=?",
                    name, email, phone, meter, type, id);
            }
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showNoSelection(); return; }
        int id   = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete customer \"" + name + "\" and all their bills?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            DatabaseHelper.getInstance().update("DELETE FROM customers WHERE id=?", id);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cannot delete: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showNoSelection() {
        JOptionPane.showMessageDialog(this, "Please select a row first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    @Override
    public void refresh() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                return DatabaseHelper.getInstance().query(
                    "SELECT id,name,email,phone,meter_id,connection_type," +
                    "TO_CHAR(created_at,'DD-Mon-YYYY') AS reg FROM customers ORDER BY id",
                    rs -> {
                        List<Object[]> rows = new ArrayList<>();
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("meter_id"),
                                rs.getString("connection_type"),
                                rs.getString("reg")
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
