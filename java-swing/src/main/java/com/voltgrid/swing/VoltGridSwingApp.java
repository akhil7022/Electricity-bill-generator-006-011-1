package com.voltgrid.swing;

import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;

import javax.swing.*;

public class VoltGridSwingApp {

    public static void main(String[] args) {
        // Install FlatLaf theme before any Swing component is created
        FlatDarkPurpleIJTheme.setup();

        // All Swing UI must run on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
