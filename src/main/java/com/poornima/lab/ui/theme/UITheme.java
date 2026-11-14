package com.poornima.lab.ui.theme;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UITheme {
    // Primary Colors
    public static final Color PRIMARY = new Color(79, 70, 229);        // Indigo-600
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);  // Indigo-700
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255); // Indigo-50

    // Neutral Colors
    public static final Color BG_MAIN = new Color(248, 250, 252);       // Slate-50
    public static final Color BG_SURFACE = Color.WHITE;
    public static final Color BG_SIDEBAR = new Color(30, 41, 59);       // Slate-800
    public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);   // Slate-700
    public static final Color SIDEBAR_SELECTED = new Color(79, 70, 229);// Indigo-600

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);     // Slate-900
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);// Slate-500
    public static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate-400
    public static final Color TEXT_SIDEBAR = new Color(226, 232, 240);  // Slate-200
    public static final Color BORDER = new Color(226, 232, 240);        // Slate-200

    // Status Colors
    public static final Color SUCCESS_TEXT = new Color(5, 150, 105);   // Emerald-600
    public static final Color SUCCESS_BG = new Color(209, 250, 229);   // Emerald-100
    public static final Color WARNING_TEXT = new Color(217, 119, 6);   // Amber-600
    public static final Color WARNING_BG = new Color(254, 243, 199);   // Amber-100
    public static final Color DANGER_TEXT = new Color(220, 38, 38);    // Red-600
    public static final Color DANGER_BG = new Color(254, 226, 226);    // Red-100
    public static final Color INFO_TEXT = new Color(37, 99, 235);      // Blue-600
    public static final Color INFO_BG = new Color(219, 234, 254);      // Blue-100

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_KPI = new Font("Segoe UI", Font.BOLD, 26);

    public static void setupFlatLaf() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("CheckBox.arc", 6);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Table.gridColor", BORDER);
            UIManager.put("TableHeader.font", FONT_BODY_BOLD);
            UIManager.put("TableHeader.height", 38);
        } catch (Exception e) {
            System.err.println("Could not initialize FlatLaf, using fallback: " + e.getMessage());
        }
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
    }
}
