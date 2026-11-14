package com.poornima.lab.ui.dashboard;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.BookingDAO;
import com.poornima.lab.dao.EquipmentDAO;
import com.poornima.lab.dao.MaintenanceDAO;
import com.poornima.lab.model.AuditLog;
import com.poornima.lab.model.Booking;
import com.poornima.lab.model.Equipment;
import com.poornima.lab.model.MaintenanceRecord;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.components.ModernTable;
import com.poornima.lab.ui.dialogs.ExportDialog;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.DateUtil;
import com.poornima.lab.util.ExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final DefaultTableModel auditModel;
    private final ModernTable auditTable;
    private final JTextArea txtSummary = new JTextArea();

    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Export & Summary Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.setBackground(UITheme.BG_SURFACE);
        topBar.setBorder(UITheme.createCardBorder());

        JLabel lblTitle = new JLabel("Audit & Export Central:");
        lblTitle.setFont(UITheme.FONT_BODY_BOLD);
        topBar.add(lblTitle);

        ModernButton btnFullDialog = new ModernButton("Open Export Dialog", ModernButton.Style.PRIMARY);
        btnFullDialog.addActionListener(e -> {
            ExportDialog dlg = new ExportDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
        });

        ModernButton btnQuickCSV = new ModernButton("Export All to CSV", ModernButton.Style.SECONDARY);
        btnQuickCSV.addActionListener(e -> quickExportAll());

        ModernButton btnRefresh = new ModernButton("Refresh Audit Logs", ModernButton.Style.SECONDARY);
        btnRefresh.addActionListener(e -> loadData());

        topBar.add(btnFullDialog);
        topBar.add(btnQuickCSV);
        topBar.add(btnRefresh);

        add(topBar, BorderLayout.NORTH);

        // 2. Center Split: Summary Text on Left, Audit Logs Table on Right
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Left: Summary Text Preview Card
        JPanel summaryCard = new JPanel(new BorderLayout(0, 10));
        summaryCard.setBackground(UITheme.BG_SURFACE);
        summaryCard.setBorder(UITheme.createCardBorder());

        JLabel lblSum = new JLabel("Executive Utilization Analytics");
        lblSum.setFont(UITheme.FONT_SUBTITLE);
        lblSum.setForeground(UITheme.TEXT_PRIMARY);
        summaryCard.add(lblSum, BorderLayout.NORTH);

        txtSummary.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtSummary.setEditable(false);
        txtSummary.setBackground(new Color(248, 250, 252));
        JScrollPane scrollSum = new JScrollPane(txtSummary);
        summaryCard.add(scrollSum, BorderLayout.CENTER);

        // Right: Recent System Audit Logs Card
        JPanel auditCard = new JPanel(new BorderLayout(0, 10));
        auditCard.setBackground(UITheme.BG_SURFACE);
        auditCard.setBorder(UITheme.createCardBorder());

        JLabel lblAud = new JLabel("Real-Time System Audit Trail");
        lblAud.setFont(UITheme.FONT_SUBTITLE);
        lblAud.setForeground(UITheme.TEXT_PRIMARY);
        auditCard.add(lblAud, BorderLayout.NORTH);

        String[] cols = {"Log ID", "User", "Action", "Entity", "Details", "Timestamp"};
        auditModel = new DefaultTableModel(cols, 0);
        auditTable = new ModernTable(auditModel);
        auditTable.getColumnModel().getColumn(0).setMaxWidth(50);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        auditTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        auditTable.getColumnModel().getColumn(5).setPreferredWidth(130);

        JScrollPane scrollAud = new JScrollPane(auditTable);
        auditCard.add(scrollAud, BorderLayout.CENTER);

        centerGrid.add(summaryCard);
        centerGrid.add(auditCard);

        add(centerGrid, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        try {
            // Load audit logs
            auditModel.setRowCount(0);
            List<AuditLog> logs = auditDAO.getRecentLogs(50);
            for (AuditLog l : logs) {
                auditModel.addRow(new Object[]{
                        l.getLogId(),
                        l.getUsername() != null ? l.getUsername() : "SYSTEM",
                        l.getAction(),
                        l.getEntity(),
                        l.getDetails(),
                        DateUtil.formatDateTime(l.getTimestamp())
                });
            }

            // Load text summary
            List<Equipment> eqList = equipmentDAO.getAllEquipment();
            List<Booking> bkList = bookingDAO.getAllBookings();
            List<MaintenanceRecord> mtList = maintenanceDAO.getAllRecords();

            txtSummary.setText(ExportUtil.generateComprehensiveReport(eqList, bkList, mtList));
            txtSummary.setCaretPosition(0);

        } catch (Exception ex) {
            System.err.println("Error loading reports data: " + ex.getMessage());
        }
    }

    private void quickExportAll() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Directory to Export CSV Files");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            try {
                ExportUtil.exportEquipmentToCSV(equipmentDAO.getAllEquipment(), new File(dir, "lab_equipment.csv"));
                ExportUtil.exportBookingsToCSV(bookingDAO.getAllBookings(), new File(dir, "lab_bookings.csv"));
                ExportUtil.exportMaintenanceToCSV(maintenanceDAO.getAllRecords(), new File(dir, "lab_maintenance.csv"));
                JOptionPane.showMessageDialog(this, "All datasets exported successfully to " + dir.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
