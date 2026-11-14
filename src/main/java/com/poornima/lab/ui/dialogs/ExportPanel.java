package com.poornima.lab.ui.dialogs;

import com.poornima.lab.dao.BookingDAO;
import com.poornima.lab.dao.EquipmentDAO;
import com.poornima.lab.dao.MaintenanceDAO;
import com.poornima.lab.model.Booking;
import com.poornima.lab.model.Equipment;
import com.poornima.lab.model.MaintenanceRecord;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.ExportUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class ExportPanel extends JPanel {
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();

    private final JTextArea txtReportPreview = new JTextArea();
    private final Window parentWindow;

    public ExportPanel(Window parentWindow) {
        this.parentWindow = parentWindow;
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Audit Reports & File Export");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.PRIMARY);

        JLabel sub = new JLabel("Export structured laboratory datasets to CSV or generate executive text summaries");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);

        ModernButton btnExportEq = new ModernButton("Export Equipment (CSV)", ModernButton.Style.SECONDARY);
        btnExportEq.addActionListener(e -> exportEquipment());

        ModernButton btnExportBook = new ModernButton("Export Bookings (CSV)", ModernButton.Style.SECONDARY);
        btnExportBook.addActionListener(e -> exportBookings());

        ModernButton btnExportMaint = new ModernButton("Export Maintenance (CSV)", ModernButton.Style.SECONDARY);
        btnExportMaint.addActionListener(e -> exportMaintenance());

        ModernButton btnRefreshReport = new ModernButton("Generate Live Report", ModernButton.Style.PRIMARY);
        btnRefreshReport.addActionListener(e -> generateReport());

        btnBar.add(btnExportEq);
        btnBar.add(btnExportBook);
        btnBar.add(btnExportMaint);
        btnBar.add(btnRefreshReport);

        // Preview Area
        JPanel previewCard = new JPanel(new BorderLayout());
        previewCard.setBackground(UITheme.BG_SURFACE);
        previewCard.setBorder(UITheme.createCardBorder());

        txtReportPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReportPreview.setEditable(false);
        txtReportPreview.setBackground(new Color(248, 250, 252));
        JScrollPane scroll = new JScrollPane(txtReportPreview);
        scroll.setPreferredSize(new Dimension(720, 340));
        previewCard.add(scroll, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        ModernButton btnSaveTxt = new ModernButton("Save Report to File", ModernButton.Style.SUCCESS);
        btnSaveTxt.addActionListener(e -> saveReportFile());

        ModernButton btnClose = new ModernButton("Close", ModernButton.Style.SECONDARY);
        btnClose.addActionListener(e -> {
            if (parentWindow != null) parentWindow.dispose();
        });

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnSaveTxt);
        rightBtns.add(btnClose);

        bottomPanel.add(rightBtns, BorderLayout.EAST);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 10));
        centerWrapper.setOpaque(false);
        centerWrapper.add(btnBar, BorderLayout.NORTH);
        centerWrapper.add(previewCard, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        generateReport();
    }

    private void generateReport() {
        try {
            List<Equipment> eqList = equipmentDAO.getAllEquipment();
            List<Booking> bkList = bookingDAO.getAllBookings();
            List<MaintenanceRecord> mtList = maintenanceDAO.getAllRecords();

            String report = ExportUtil.generateComprehensiveReport(eqList, bkList, mtList);
            txtReportPreview.setText(report);
            txtReportPreview.setCaretPosition(0);
        } catch (Exception ex) {
            txtReportPreview.setText("Error generating report: " + ex.getMessage());
        }
    }

    private void exportEquipment() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("equipment_catalog.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<Equipment> list = equipmentDAO.getAllEquipment();
                ExportUtil.exportEquipmentToCSV(list, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Equipment catalog exported successfully!", "Export Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportBookings() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("bookings_report.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<Booking> list = bookingDAO.getAllBookings();
                ExportUtil.exportBookingsToCSV(list, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Bookings history exported successfully!", "Export Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportMaintenance() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("maintenance_logs.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<MaintenanceRecord> list = maintenanceDAO.getAllRecords();
                ExportUtil.exportMaintenanceToCSV(list, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Maintenance logs exported successfully!", "Export Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveReportFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("lab_audit_report.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
                pw.write(txtReportPreview.getText());
                JOptionPane.showMessageDialog(this, "Audit report saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
