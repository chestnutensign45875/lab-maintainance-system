package com.poornima.lab.ui.components;

import com.poornima.lab.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

public class StatCard extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final Color accentColor;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(8, 6));
        setBackground(UITheme.BG_SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_BODY);
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);

        lblValue = new JLabel(value);
        lblValue.setFont(UITheme.FONT_KPI);
        lblValue.setForeground(UITheme.TEXT_PRIMARY);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(lblValue);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(lblSubtitle);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(accentColor);
        // Draw top accent indicator
        g2.fillRect(0, 0, getWidth(), 4);
        g2.dispose();
    }
}
