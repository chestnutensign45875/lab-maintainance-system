package com.poornima.lab;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.ui.auth.LoginFrame;
import com.poornima.lab.ui.theme.UITheme;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Optimize Swing rendering and fonts
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize UI Theme (FlatLaf Modern Look & Feel)
        UITheme.setupFlatLaf();

        // Initialize Database & Seeds
        try {
            DatabaseConfig.initialize();
            LOGGER.info("Database initialized successfully (" + DatabaseConfig.getDbType() + ")");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            JOptionPane.showMessageDialog(null,
                    "Database initialization error:\n" + e.getMessage() +
                    "\nPlease verify MySQL service or local permissions.",
                    "Fatal Startup Error", JOptionPane.ERROR_MESSAGE);
        }

        // Launch GUI
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error launching application", e);
            }
        });
    }
}
