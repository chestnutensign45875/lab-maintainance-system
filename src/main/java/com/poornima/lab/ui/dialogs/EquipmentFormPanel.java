package com.poornima.lab.ui.dialogs;

import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.CategoryDAO;
import com.poornima.lab.dao.EquipmentDAO;
import com.poornima.lab.model.Category;
import com.poornima.lab.model.Equipment;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.DateUtil;
import com.poornima.lab.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class EquipmentFormPanel extends JPanel {
    private final JTextField txtAssetTag = new JTextField(18);
    private final JTextField txtName = new JTextField(18);
    private final JComboBox<Category> cmbCategory = new JComboBox<>();
    private final JTextField txtModel = new JTextField(18);
    private final JTextField txtSerial = new JTextField(18);
    private final JComboBox<String> cmbRoom = new JComboBox<>(new String[]{
            "Lab 301 - AI & Vision Lab",
            "Lab 204 - Embedded Systems",
            "Lab 102 - Advanced Networks",
            "Lab 302 - IoT Innovations",
            "Lab 206 - RF & Microwave",
            "Lab 108 - Robotics Lab",
            "Lab 108 - Rapid Prototyping",
            "Lab 205 - Signal Processing"
    });
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
            "AVAILABLE", "BOOKED", "UNDER_MAINTENANCE", "DECOMMISSIONED"
    });
    private final JTextField txtPurchaseDate = new JTextField(18);
    private final JTextArea txtNotes = new JTextArea(3, 18);

    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    private final Equipment existingEquipment;
    private final User currentUser;
    private final Window parentWindow;
    private boolean saved = false;

    public EquipmentFormPanel(Window parentWindow, Equipment equipment, User currentUser) {
        this.parentWindow = parentWindow;
        this.existingEquipment = equipment;
        this.currentUser = currentUser;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        loadCategories();

        JLabel title = new JLabel(equipment == null ? "Register New Equipment" : "Modify Equipment Record");
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
        addFormField(formCard, gbc, row++, "Asset Tag (e.g. EQ-HPC-01):", txtAssetTag);
        addFormField(formCard, gbc, row++, "Equipment Name:", txtName);
        addFormField(formCard, gbc, row++, "Category:", cmbCategory);
        addFormField(formCard, gbc, row++, "Hardware Model / Specs:", txtModel);
        addFormField(formCard, gbc, row++, "Serial Number:", txtSerial);
        addFormField(formCard, gbc, row++, "Assigned Lab Room:", cmbRoom);
        addFormField(formCard, gbc, row++, "Operational Status:", cmbStatus);
        addFormField(formCard, gbc, row++, "Purchase Date (yyyy-MM-dd):", txtPurchaseDate);

        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane scrollNotes = new JScrollPane(txtNotes);
        addFormField(formCard, gbc, row++, "Condition / Technical Notes:", scrollNotes);

        add(formCard, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.Style.SECONDARY);
        btnCancel.addActionListener(e -> {
            if (parentWindow != null) parentWindow.dispose();
        });

        ModernButton btnSave = new ModernButton(equipment == null ? "Save Asset" : "Update Asset", ModernButton.Style.PRIMARY);
        btnSave.addActionListener(e -> handleSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);

        if (existingEquipment != null) {
            populateFields();
        } else {
            txtPurchaseDate.setText(DateUtil.formatStandardDate(new Date(System.currentTimeMillis())));
        }
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            for (Category c : categories) {
                cmbCategory.addItem(c);
            }
        } catch (Exception e) {
            System.err.println("Could not load categories: " + e.getMessage());
        }
    }

    private void populateFields() {
        txtAssetTag.setText(existingEquipment.getAssetTag());
        txtName.setText(existingEquipment.getName());
        txtModel.setText(existingEquipment.getModel());
        txtSerial.setText(existingEquipment.getSerialNumber());
        cmbRoom.setSelectedItem(existingEquipment.getLabRoom());
        cmbStatus.setSelectedItem(existingEquipment.getStatus());
        if (existingEquipment.getPurchaseDate() != null) {
            txtPurchaseDate.setText(DateUtil.formatStandardDate(existingEquipment.getPurchaseDate()));
        }
        txtNotes.setText(existingEquipment.getConditionNotes());

        for (int i = 0; i < cmbCategory.getItemCount(); i++) {
            if (cmbCategory.getItemAt(i).getCategoryId() == existingEquipment.getCategoryId()) {
                cmbCategory.setSelectedIndex(i);
                break;
            }
        }
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
        String tag = txtAssetTag.getText().trim();
        String name = txtName.getText().trim();
        Category cat = (Category) cmbCategory.getSelectedItem();
        String model = txtModel.getText().trim();
        String serial = txtSerial.getText().trim();
        String room = (String) cmbRoom.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();
        String pDateStr = txtPurchaseDate.getText().trim();
        String notes = txtNotes.getText().trim();

        if (ValidationUtil.isEmpty(tag) || ValidationUtil.isEmpty(name) || cat == null) {
            JOptionPane.showMessageDialog(this, "Asset Tag, Equipment Name, and Category are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date purchaseDate = null;
        if (!ValidationUtil.isEmpty(pDateStr)) {
            try {
                purchaseDate = DateUtil.parseDate(pDateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter purchase date in yyyy-MM-dd format.", "Date Format Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            if (existingEquipment == null) {
                if (equipmentDAO.findByAssetTag(tag) != null) {
                    JOptionPane.showMessageDialog(this, "Asset tag '" + tag + "' already exists.", "Duplicate Asset Tag", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Equipment eq = new Equipment(0, tag, name, cat.getCategoryId(), model, serial, room, status, purchaseDate, notes);
                if (equipmentDAO.create(eq)) {
                    auditDAO.log(currentUser != null ? currentUser.getUserId() : null, "EQUIPMENT_ADDED", "EQUIPMENT", eq.getEquipmentId(), "Added: " + tag);
                    JOptionPane.showMessageDialog(this, "Equipment registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    saved = true;
                    if (parentWindow != null) parentWindow.dispose();
                }
            } else {
                existingEquipment.setAssetTag(tag);
                existingEquipment.setName(name);
                existingEquipment.setCategoryId(cat.getCategoryId());
                existingEquipment.setModel(model);
                existingEquipment.setSerialNumber(serial);
                existingEquipment.setLabRoom(room);
                existingEquipment.setStatus(status);
                existingEquipment.setPurchaseDate(purchaseDate);
                existingEquipment.setConditionNotes(notes);

                if (equipmentDAO.update(existingEquipment)) {
                    auditDAO.log(currentUser != null ? currentUser.getUserId() : null, "EQUIPMENT_UPDATED", "EQUIPMENT", existingEquipment.getEquipmentId(), "Updated: " + tag);
                    JOptionPane.showMessageDialog(this, "Equipment updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
