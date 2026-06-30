package com.voltgrid.swing.util;

import java.awt.*;

public final class UIColors {
    private UIColors() {}

    public static final Color SIDEBAR_BG     = new Color(30, 27, 50);
    public static final Color SIDEBAR_ACTIVE = new Color(99, 75, 255);
    public static final Color SIDEBAR_HOVER  = new Color(50, 45, 80);
    public static final Color ACCENT_BLUE    = new Color(99, 75, 255);
    public static final Color SUCCESS        = new Color(34, 197, 94);
    public static final Color WARNING        = new Color(234, 179, 8);
    public static final Color DANGER         = new Color(239, 68, 68);
    public static final Color CARD_BG        = new Color(40, 37, 65);
    public static final Color TEXT_PRIMARY   = new Color(240, 240, 255);
    public static final Color TEXT_SECONDARY = new Color(160, 155, 200);

    public static Color statusColor(String status) {
        if (status == null) return TEXT_SECONDARY;
        return switch (status.toUpperCase()) {
            case "PAID"    -> SUCCESS;
            case "OVERDUE" -> DANGER;
            default        -> WARNING;
        };
    }
}
