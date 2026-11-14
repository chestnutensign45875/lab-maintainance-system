package com.poornima.lab.ui.components;

import com.poornima.lab.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

public class ModernButton extends JButton {
    public enum Style {
        PRIMARY, SECONDARY, DANGER, SUCCESS
    }

    private final Style style;

    public ModernButton(String text, Style style) {
        super(text);
        this.style = style;
        setFont(UITheme.FONT_BODY_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        applyStyling();
    }

    public ModernButton(String text) {
        this(text, Style.PRIMARY);
    }

    private void applyStyling() {
        switch (style) {
            case PRIMARY:
                setBackground(UITheme.PRIMARY);
                setForeground(Color.WHITE);
                break;
            case SECONDARY:
                setBackground(new Color(241, 245, 249)); // Slate-100
                setForeground(UITheme.TEXT_PRIMARY);
                setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
                setBorderPainted(true);
                break;
            case DANGER:
                setBackground(new Color(239, 68, 68));   // Red-500
                setForeground(Color.WHITE);
                break;
            case SUCCESS:
                setBackground(new Color(16, 185, 129));  // Emerald-500
                setForeground(Color.WHITE);
                break;
        }
    }
}
