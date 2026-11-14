package com.poornima.lab.ui.components;

import com.poornima.lab.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ModernTable extends JTable {

    public ModernTable(DefaultTableModel model) {
        super(model);
        setFont(UITheme.FONT_BODY);
        setRowHeight(36);
        setShowGrid(true);
        setGridColor(UITheme.BORDER);
        setSelectionBackground(UITheme.PRIMARY_LIGHT);
        setSelectionForeground(UITheme.TEXT_PRIMARY);
        setFillsViewportHeight(true);
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setBackground(new Color(241, 245, 249));
        getTableHeader().setForeground(UITheme.TEXT_PRIMARY);
        getTableHeader().setFont(UITheme.FONT_BODY_BOLD);

        // Center align integer IDs
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Padding renderer for text cells
        DefaultTableCellRenderer paddingRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_SURFACE : new Color(249, 250, 251));
                }
                return c;
            }
        };
        setDefaultRenderer(Object.class, paddingRenderer);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
