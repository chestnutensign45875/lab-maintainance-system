package com.poornima.lab.ui.dialogs;

import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.EquipmentDAO;
import com.poornima.lab.dao.MaintenanceDAO;
import com.poornima.lab.model.Equipment;
import com.poornima.lab.model.MaintenanceRecord;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.DateUtil;
import com.poornima.lab.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class MaintenanceFormPanel extends JPanel {
    private final JComboBox<Equipment> cmbEquipment = new JComboBox<>();
    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{
            "PREVENTIVE", "CALIBRATION", "REPAIR", "INSPECTION", "FIRMWARE_UPGRADE"
    });
    private final JTextField txtStartDate = new JTextField(18);
    private final JTextField txtCompletionDate = new JTextField(18);
    private final JTextField txtTechnician = new JTextField(18);
    private final JTextField txtCost = new JTextField(18);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
            "IN_PROGRESS", "SCHEDULED", "COMPLETED", "CANCELLED"
    });
    private final JTextArea txtNotes = new JTextArea(3, 18);

    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    private final MaintenanceRecord existingRecord;
    private final User currentUser;
    private final Window parentWindow;
    private boolean saved = false;

    public MaintenanceFormPanel(Window parentWindow, MaintenanceRecord record, User currentUser) {
        this.parentWindow = parentWindow;
        this.existingRecord = record;
        this.currentUser = currentUser;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        loadEquipment();

        JLabel title = new JLabel(record == null ? "Open Maintenance Service Ticket" : "Modify Service Ticket #" + record.getRecordId());
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UITheme.BG_SURFACE);
        formCard.setBorder(UITheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 1.0;

        int row = 0;
        addFormField(formCard, gbc, row++, "Equipment Asset:", cmbEquipment);
        addFormField(formCard, gbc, row++, "Maintenance Category:", cmbType);
        addFormField(formCard, gbc, row++, "Start Date (yyyy-MM-dd):", txtStartDate);
        addFormField(formCard, gbc, row++, "Completion Date (optional):", txtCompletionDate);
        addFormField(formCard, gbc, row++, "Assigned Technician / Vendor:", txtTechnician);
        addFormField(formCard, gbc, row++, "Estimated / Actual Cost (₹):", txtCost);
        addFormField(formCard, gbc, row++, "Service Status:", cmbStatus);

        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane scrollNotes = new JScrollPane(txtNotes);
        addFormField(formCard, gbc, row++, "Service Diagnosis & Findings:", scrollNotes);

        add(formCard, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.Style.SECONDARY);
        btnCancel.addActionListener(e -> {
            if (parentWindow != null) parentWindow.dispose();
        });

        ModernButton btnSave = new ModernButton(record == null ? "Create Ticket" : "Update Ticket", ModernButton.Style.PRIMARY);
        btnSave.addActionListener(e -> handleSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);

        if (existingRecord != null) {
            populateFields();
        } else {
            txtStartDate.setText(DateUtil.formatStandardDate(new Date(System.currentTimeMillis())));
            txtCost.setText("0.00");
            txtTechnician.setText("Mr. Rajesh Kumar (Lab Assistant)");
        }
    }

    private void loadEquipment() {
        try {
            List<Equipment> list = equipmentDAO.getAllEquipment();
            for (Equipment e : list) {
                cmbEquipment.addItem(e);
            }
        } catch (Exception e) {
            System.err.println("Could not load equipment list: " + e.getMessage());
        }
    }

    private void populateFields() {
        for (int i = 0; i < cmbEquipment.getItemCount(); i++) {
            if (cmbEquipment.getItemAt(i).getEquipmentId() == existingRecord.getEquipmentId()) {
                cmbEquipment.setSelectedIndex(i);
                break;
            }
        }
        cmbType.setSelectedItem(existingRecord.getMaintenanceType());
        if (existingRecord.getStartDate() != null) {
            txtStartDate.setText(DateUtil.formatStandardDate(existingRecord.getStartDate()));
        }
        if (existingRecord.getCompletionDate() != null) {
            txtCompletionDate.setText(DateUtil.formatStandardDate(existingRecord.getCompletionDate()));
        }
        txtTechnician.setText(existingRecord.getTechnicianName());
        txtCost.setText(existingRecord.getCost() != null ? existingRecord.getCost().toString() : "0.00");
        cmbStatus.setSelectedItem(existingRecord.getStatus());
        txtNotes.setText(existingRecord.getNotes());
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(comp, gbc);
    }

    private void handleSave() {
        Equipment eq = (Equipment) cmbEquipment.getSelectedItem();
        String type = (String) cmbType.getSelectedItem();
        String startStr = txtStartDate.getText().trim();
        String compStr = txtCompletionDate.getText().trim();
        String tech = txtTechnician.getText().trim();
        String costStr = txtCost.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        String notes = txtNotes.getText().trim();

        if (eq == null || ValidationUtil.isEmpty(startStr) || ValidationUtil.isEmpty(tech)) {
            JOptionPane.showMessageDialog(this, "Equipment, Start Date, and Technician are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date startDate, compDate = null;
        try {
            startDate = DateUtil.parseDate(startStr);
            if (!ValidationUtil.isEmpty(compStr)) {
                compDate = DateUtil.parseDate(compStr);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.", "Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal cost = BigDecimal.ZERO;
        try {
            if (!ValidationUtil.isEmpty(costStr)) {
                cost = new BigDecimal(costStr);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cost must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (existingRecord == null) {
                MaintenanceRecord rec = new MaintenanceRecord(0, eq.getEquipmentId(), type, startDate, compDate, tech, cost, status, notes);
                if (maintenanceDAO.create(rec)) {
                    if ("IN_PROGRESS".equalsIgnoreCase(status) || "SCHEDULED".equalsIgnoreCase(status)) {
                        equipmentDAO.updateStatus(eq.getEquipmentId(), "UNDER_MAINTENANCE");
                    } else if ("COMPLETED".equalsIgnoreCase(status)) {
                        equipmentDAO.updateStatus(eq.getEquipmentId(), "AVAILABLE");
                    }

                    auditDAO.log(currentUser != null ? currentUser.getUserId() : null, "MAINTENANCE_LOGGED", "MAINTENANCE",
                            rec.getRecordId(), "Logged " + type + " for " + eq.getAssetTag());

                    JOptionPane.showMessageDialog(this, "Maintenance record created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    saved = true;
                    if (parentWindow != null) parentWindow.dispose();
                }
            } else {
                existingRecord.setEquipmentId(eq.getEquipmentId());
                existingRecord.setMaintenanceType(type);
                existingRecord.setStartDate(startDate);
                existingRecord.setCompletionDate(compDate);
                existingRecord.setTechnicianName(tech);
                existingRecord.setCost(cost);
                existingRecord.setStatus(status);
                existingRecord.setNotes(notes);

                if (maintenanceDAO.update(existingRecord)) {
                    if ("COMPLETED".equalsIgnoreCase(status)) {
                        equipmentDAO.updateStatus(eq.getEquipmentId(), "AVAILABLE");
                    } else if ("IN_PROGRESS".equalsIgnoreCase(status) || "SCHEDULED".equalsIgnoreCase(status)) {
                        equipmentDAO.updateStatus(eq.getEquipmentId(), "UNDER_MAINTENANCE");
                    }

                    auditDAO.log(currentUser != null ? currentUser.getUserId() : null, "MAINTENANCE_UPDATED", "MAINTENANCE",
                            existingRecord.getRecordId(), "Updated ticket #" + existingRecord.getRecordId());

                    JOptionPane.showMessageDialog(this, "Maintenance record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    saved = true;
                    if (parentWindow != null) parentWindow.dispose();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
