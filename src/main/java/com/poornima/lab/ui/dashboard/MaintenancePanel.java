package com.poornima.lab.ui.dashboard;

import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.EquipmentDAO;
import com.poornima.lab.dao.MaintenanceDAO;
import com.poornima.lab.model.MaintenanceRecord;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.components.ModernTable;
import com.poornima.lab.ui.components.StatusBadgeRenderer;
import com.poornima.lab.ui.dialogs.MaintenanceDialog;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.DateUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class MaintenancePanel extends JPanel {
    private final JTextField txtSearch = new JTextField(18);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
            "ALL", "IN_PROGRESS", "SCHEDULED", "COMPLETED", "CANCELLED"
    });

    private final DefaultTableModel tableModel;
    private final ModernTable table;

    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    private final User currentUser;
    private List<MaintenanceRecord> currentList;

    public MaintenancePanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Filters & Search
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(UITheme.BG_SURFACE);
        filterCard.setBorder(UITheme.createCardBorder());

        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(lblSearch);
        txtSearch.setToolTipText("Search by asset tag, equipment name, technician, or maintenance type...");
        filterCard.add(txtSearch);

        JLabel lblStatus = new JLabel("Filter Status:");
        lblStatus.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(lblStatus);
        filterCard.add(cmbStatus);

        ModernButton btnReset = new ModernButton("Reset", ModernButton.Style.SECONDARY);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatus.setSelectedIndex(0);
            loadData();
        });
        filterCard.add(btnReset);

        add(filterCard, BorderLayout.NORTH);

        // 2. Center Table
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(UITheme.BG_SURFACE);
        tableCard.setBorder(UITheme.createCardBorder());

        String[] cols = {"Ticket #", "Asset Tag", "Equipment Name", "Service Type", "Start Date", "Completed", "Technician", "Cost (₹)", "Status", "Notes"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new ModernTable(tableModel);

        table.getColumnModel().getColumn(0).setMaxWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(95);
        table.getColumnModel().getColumn(2).setPreferredWidth(190);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);
        table.getColumnModel().getColumn(8).setPreferredWidth(120);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons at Bottom
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionPanel.setOpaque(false);

        if (currentUser.isAdmin() || currentUser.isTechnician()) {
            ModernButton btnNew = new ModernButton("+ Open Service Ticket", ModernButton.Style.PRIMARY);
            btnNew.addActionListener(e -> handleNewRecord());

            ModernButton btnEdit = new ModernButton("Edit Ticket", ModernButton.Style.SECONDARY);
            btnEdit.addActionListener(e -> handleEditRecord());

            ModernButton btnComplete = new ModernButton("Mark Service Completed", ModernButton.Style.SUCCESS);
            btnComplete.addActionListener(e -> handleCompleteRecord());

            actionPanel.add(btnNew);
            actionPanel.add(btnEdit);
            actionPanel.add(btnComplete);
        }

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.Style.SECONDARY);
        btnRefresh.addActionListener(e -> loadData());
        actionPanel.add(btnRefresh);

        tableCard.add(actionPanel, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        // Search listeners
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });
        cmbStatus.addActionListener(e -> loadData());

        loadData();
    }

    public void loadData() {
        try {
            String kw = txtSearch.getText().trim();
            String status = (String) cmbStatus.getSelectedItem();

            currentList = maintenanceDAO.searchRecords(kw, status);
            tableModel.setRowCount(0);

            for (MaintenanceRecord r : currentList) {
                tableModel.addRow(new Object[]{
                        r.getRecordId(),
                        r.getAssetTag(),
                        r.getEquipmentName(),
                        r.getMaintenanceType(),
                        DateUtil.formatDate(r.getStartDate()),
                        r.getCompletionDate() != null ? DateUtil.formatDate(r.getCompletionDate()) : "In-Service",
                        r.getTechnicianName(),
                        r.getCost() != null ? String.format("%.2f", r.getCost().doubleValue()) : "0.00",
                        r.getStatus(),
                        r.getNotes() != null ? r.getNotes() : ""
                });
            }
        } catch (Exception ex) {
            System.err.println("Error loading maintenance data: " + ex.getMessage());
        }
    }

    private MaintenanceRecord getSelectedRecord() {
        int row = table.getSelectedRow();
        if (row == -1 || currentList == null || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a maintenance ticket from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void handleNewRecord() {
        MaintenanceDialog dlg = new MaintenanceDialog(SwingUtilities.getWindowAncestor(this), null, currentUser);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void handleEditRecord() {
        MaintenanceRecord r = getSelectedRecord();
        if (r == null) return;

        MaintenanceDialog dlg = new MaintenanceDialog(SwingUtilities.getWindowAncestor(this), r, currentUser);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void handleCompleteRecord() {
        MaintenanceRecord r = getSelectedRecord();
        if (r == null) return;

        if ("COMPLETED".equalsIgnoreCase(r.getStatus())) {
            JOptionPane.showMessageDialog(this, "This maintenance ticket is already closed and marked COMPLETED.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String notes = JOptionPane.showInputDialog(this,
                "Enter completion remarks & inspection sign-off for '" + r.getEquipmentName() + "':",
                "Service Sign-Off", JOptionPane.QUESTION_MESSAGE);

        if (notes != null) {
            try {
                Date today = new Date(System.currentTimeMillis());
                if (maintenanceDAO.completeMaintenance(r.getRecordId(), today, notes.trim())) {
                    equipmentDAO.updateStatus(r.getEquipmentId(), "AVAILABLE");
                    auditDAO.log(currentUser.getUserId(), "MAINTENANCE_COMPLETED", "MAINTENANCE", r.getRecordId(), "Completed maintenance #" + r.getRecordId());
                    JOptionPane.showMessageDialog(this, "Maintenance ticket closed successfully! Equipment restored to AVAILABLE status.", "Service Complete", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error completing maintenance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
