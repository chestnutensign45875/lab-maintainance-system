package com.poornima.lab.ui.components;

import com.poornima.lab.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusBadgeRenderer extends DefaultTableCellRenderer {
    private String currentText = "";
    private Color textColor = UITheme.TEXT_PRIMARY;
    private Color bgColor = UITheme.BG_MAIN;

    public StatusBadgeRenderer() {
        setHorizontalAlignment(CENTER);
        setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        currentText = value != null ? value.toString() : "";
        setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));

        String upper = currentText.toUpperCase();
        if (upper.contains("AVAILABLE") || upper.contains("ACTIVE") || upper.contains("APPROVED")) {
            textColor = UITheme.SUCCESS_TEXT;
            bgColor = UITheme.SUCCESS_BG;
        } else if (upper.contains("BOOKED") || upper.contains("PENDING") || upper.contains("SCHEDULED")) {
            textColor = UITheme.WARNING_TEXT;
            bgColor = UITheme.WARNING_BG;
        } else if (upper.contains("MAINTENANCE") || upper.contains("REJECTED") || upper.contains("BLOCKED")) {
            textColor = UITheme.DANGER_TEXT;
            bgColor = UITheme.DANGER_BG;
        } else if (upper.contains("COMPLETED") || upper.contains("IN_PROGRESS")) {
            textColor = UITheme.INFO_TEXT;
            bgColor = UITheme.INFO_BG;
        } else {
            textColor = UITheme.TEXT_SECONDARY;
            bgColor = new Color(241, 245, 249);
        }

        setText(currentText.replace('_', ' '));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw pill background
        int marginX = 8;
        int marginY = 5;
        int width = getWidth() - (marginX * 2);
        int height = getHeight() - (marginY * 2);

        g2.setColor(bgColor);
        g2.fillRoundRect(marginX, marginY, width, height, 12, 12);

        // Draw text
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics(getFont());
        int textWidth = fm.stringWidth(getText());
        int textX = (getWidth() - textWidth) / 2;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        g2.setFont(getFont());
        g2.drawString(getText(), textX, textY);
        g2.dispose();
    }
}
