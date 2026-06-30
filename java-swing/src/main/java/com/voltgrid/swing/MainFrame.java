package com.voltgrid.swing;

import com.voltgrid.swing.panel.*;
import com.voltgrid.swing.util.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main application window.
 * Left: dark sidebar with nav buttons.
 * Right: CardLayout content area that swaps panels.
 */
public class MainFrame extends JFrame {

    private static final int SIDEBAR_W = 220;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);

    private JLabel activeLabel = null;

    public MainFrame() {
        super("⚡ VoltGrid Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(contentArea,   BorderLayout.CENTER);

        // Register panels
        addPanel("dashboard",  new DashboardPanel());
        addPanel("customers",  new CustomersPanel());
        addPanel("meter",      new MeterReadingsPanel());
        addPanel("bills",      new BillsPanel());
        addPanel("tariffs",    new TariffsPanel());

        // Show dashboard on launch
        cardLayout.show(contentArea, "dashboard");
    }

    private void addPanel(String key, JPanel panel) {
        contentArea.add(panel, key);
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UIColors.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        logo.setBackground(UIColors.SIDEBAR_BG);
        logo.setMaximumSize(new Dimension(SIDEBAR_W, 70));
        logo.setMinimumSize(new Dimension(SIDEBAR_W, 70));

        JLabel icon = new JLabel("⚡");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icon.setForeground(UIColors.ACCENT_BLUE);

        JLabel title = new JLabel("VoltGrid");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIColors.TEXT_PRIMARY);

        logo.add(icon);
        logo.add(title);
        sidebar.add(logo);

        // Divider
        sidebar.add(divider());
        sidebar.add(Box.createVerticalStrut(8));

        // Nav items
        String[][] items = {
            {"dashboard", "📊  Dashboard"},
            {"customers", "👤  Customers"},
            {"meter",     "🔌  Meter Readings"},
            {"bills",     "📄  Bills"},
            {"tariffs",   "⚙️   Tariffs"},
        };

        for (String[] item : items) {
            JLabel lbl = navLabel(item[1], item[0]);
            sidebar.add(lbl);
        }

        sidebar.add(Box.createVerticalGlue());

        // Footer
        JLabel footer = new JLabel("  VoltGrid v1.0");
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footer.setForeground(UIColors.TEXT_SECONDARY);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 0));
        sidebar.add(footer);

        return sidebar;
    }

    private JLabel navLabel(String text, String panelKey) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UIColors.TEXT_SECONDARY);
        lbl.setMaximumSize(new Dimension(SIDEBAR_W, 44));
        lbl.setPreferredSize(new Dimension(SIDEBAR_W, 44));
        lbl.setOpaque(true);
        lbl.setBackground(UIColors.SIDEBAR_BG);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if ("dashboard".equals(panelKey)) {
            setActive(lbl);
        }

        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                setActive(lbl);
                cardLayout.show(contentArea, panelKey);
                // Refresh data on panel switch
                Component shown = getVisiblePanel();
                if (shown instanceof Refreshable r) r.refresh();
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (lbl != activeLabel)
                    lbl.setBackground(UIColors.SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (lbl != activeLabel)
                    lbl.setBackground(UIColors.SIDEBAR_BG);
            }
        });
        return lbl;
    }

    private void setActive(JLabel lbl) {
        if (activeLabel != null) {
            activeLabel.setBackground(UIColors.SIDEBAR_BG);
            activeLabel.setForeground(UIColors.TEXT_SECONDARY);
            activeLabel.setFont(activeLabel.getFont().deriveFont(Font.PLAIN));
        }
        activeLabel = lbl;
        lbl.setBackground(UIColors.SIDEBAR_ACTIVE);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
    }

    private Component getVisiblePanel() {
        for (Component c : contentArea.getComponents()) {
            if (c.isVisible()) return c;
        }
        return null;
    }

    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UIColors.SIDEBAR_HOVER);
        sep.setMaximumSize(new Dimension(SIDEBAR_W, 1));
        return sep;
    }
}
