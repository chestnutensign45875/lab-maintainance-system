package com.poornima.lab.ui.dashboard;

import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.CategoryDAO;
import com.poornima.lab.dao.EquipmentDAO;
import com.poornima.lab.model.Category;
import com.poornima.lab.model.Equipment;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.components.ModernTable;
import com.poornima.lab.ui.components.StatusBadgeRenderer;
import com.poornima.lab.ui.dialogs.BookingDialog;
import com.poornima.lab.ui.dialogs.EquipmentDialog;
import com.poornima.lab.ui.dialogs.MaintenanceDialog;
import com.poornima.lab.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class EquipmentPanel extends JPanel {
    private final JTextField txtSearch = new JTextField(16);
    private final JComboBox<String> cmbCategory = new JComboBox<>();
    private final JComboBox<String> cmbRoom = new JComboBox<>();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
            "ALL", "AVAILABLE", "BOOKED", "UNDER_MAINTENANCE", "DECOMMISSIONED"
    });

    private final DefaultTableModel tableModel;
    private final ModernTable table;

    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    private final User currentUser;
    private List<Equipment> currentList;

    public EquipmentPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Filters & Search Bar Card
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(UITheme.BG_SURFACE);
        filterCard.setBorder(UITheme.createCardBorder());

        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(lblSearch);
        txtSearch.setToolTipText("Search by asset tag, name, model, or serial...");
        filterCard.add(txtSearch);

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(lblCat);
        cmbCategory.addItem("ALL");
        loadCategories();
        filterCard.add(cmbCategory);

        JLabel lblRoom = new JLabel("Lab Room:");
        lblRoom.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(lblRoom);
        cmbRoom.addItem("ALL");
        loadRooms();
        filterCard.add(cmbRoom);

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(lblStatus);
        filterCard.add(cmbStatus);

        ModernButton btnReset = new ModernButton("Reset", ModernButton.Style.SECONDARY);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategory.setSelectedIndex(0);
            cmbRoom.setSelectedIndex(0);
            cmbStatus.setSelectedIndex(0);
            loadData();
        });
        filterCard.add(btnReset);

        add(filterCard, BorderLayout.NORTH);

        // 2. Center Table
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(UITheme.BG_SURFACE);
        tableCard.setBorder(UITheme.createCardBorder());

        String[] cols = {"ID", "Asset Tag", "Equipment Name", "Category", "Model / Specs", "Lab Room", "Status", "Notes"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new ModernTable(tableModel);

        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(95);
        table.getColumnModel().getColumn(2).setPreferredWidth(210);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    handleEdit();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons at Bottom of Table Card
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionPanel.setOpaque(false);

        ModernButton btnReserve = new ModernButton("Reserve Selected", ModernButton.Style.PRIMARY);
        btnReserve.addActionListener(e -> handleReserve());
        actionPanel.add(btnReserve);

        if (currentUser.isAdmin() || currentUser.isTechnician()) {
            ModernButton btnAdd = new ModernButton("+ Add Asset", ModernButton.Style.SUCCESS);
            btnAdd.addActionListener(e -> handleAdd());

            ModernButton btnEdit = new ModernButton("Edit Asset", ModernButton.Style.SECONDARY);
            btnEdit.addActionListener(e -> handleEdit());

            ModernButton btnMaint = new ModernButton("Log Maintenance", ModernButton.Style.SECONDARY);
            btnMaint.addActionListener(e -> handleMaintenance());

            actionPanel.add(btnAdd);
            actionPanel.add(btnEdit);
            actionPanel.add(btnMaint);
        }

        if (currentUser.isAdmin()) {
            ModernButton btnDelete = new ModernButton("Delete Asset", ModernButton.Style.DANGER);
            btnDelete.addActionListener(e -> handleDelete());
            actionPanel.add(btnDelete);
        }

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.Style.SECONDARY);
        btnRefresh.addActionListener(e -> loadData());
        actionPanel.add(btnRefresh);

        tableCard.add(actionPanel, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        // Listeners for live filtering
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });
        cmbCategory.addActionListener(e -> loadData());
        cmbRoom.addActionListener(e -> loadData());
        cmbStatus.addActionListener(e -> loadData());

        loadData();
    }

    private void loadCategories() {
        try {
            List<Category> list = categoryDAO.getAllCategories();
            for (Category c : list) {
                cmbCategory.addItem(c.getName());
            }
        } catch (Exception ignored) {}
    }

    private void loadRooms() {
        try {
            List<String> rooms = equipmentDAO.getAllLabRooms();
            for (String r : rooms) {
                cmbRoom.addItem(r);
            }
        } catch (Exception ignored) {}
    }

    public void loadData() {
        try {
            String kw = txtSearch.getText().trim();
            String catName = (String) cmbCategory.getSelectedItem();
            Integer catId = null;
            if (catName != null && !"ALL".equalsIgnoreCase(catName)) {
                List<Category> list = categoryDAO.getAllCategories();
                for (Category c : list) {
                    if (c.getName().equalsIgnoreCase(catName)) {
                        catId = c.getCategoryId();
                        break;
                    }
                }
            }

            String room = (String) cmbRoom.getSelectedItem();
            String status = (String) cmbStatus.getSelectedItem();

            currentList = equipmentDAO.searchEquipment(kw, catId, status, room);
            tableModel.setRowCount(0);

            for (Equipment e : currentList) {
                tableModel.addRow(new Object[]{
                        e.getEquipmentId(),
                        e.getAssetTag(),
                        e.getName(),
                        e.getCategoryName() != null ? e.getCategoryName() : "General",
                        e.getModel() != null ? e.getModel() : "-",
                        e.getLabRoom(),
                        e.getStatus(),
                        e.getConditionNotes() != null ? e.getConditionNotes() : ""
                });
            }
        } catch (Exception ex) {
            System.err.println("Error loading equipment data: " + ex.getMessage());
        }
    }

    private Equipment getSelectedEquipment() {
        int row = table.getSelectedRow();
        if (row == -1 || currentList == null || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an equipment record from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void handleReserve() {
        Equipment eq = getSelectedEquipment();
        if (eq == null) return;

        if (!eq.isAvailable()) {
            JOptionPane.showMessageDialog(this, "Equipment '" + eq.getName() + "' is currently " + eq.getStatus() + ".\n" +
                    "It cannot be checked out at this time.", "Equipment Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookingDialog dlg = new BookingDialog(SwingUtilities.getWindowAncestor(this), currentUser, eq);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void handleAdd() {
        EquipmentDialog dlg = new EquipmentDialog(SwingUtilities.getWindowAncestor(this), null, currentUser);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void handleEdit() {
        Equipment eq = getSelectedEquipment();
        if (eq == null) return;

        EquipmentDialog dlg = new EquipmentDialog(SwingUtilities.getWindowAncestor(this), eq, currentUser);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void handleMaintenance() {
        Equipment eq = getSelectedEquipment();
        if (eq == null) return;

        MaintenanceDialog dlg = new MaintenanceDialog(SwingUtilities.getWindowAncestor(this), null, currentUser);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void handleDelete() {
        Equipment eq = getSelectedEquipment();
        if (eq == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete equipment '" + eq.getName() + "' (" + eq.getAssetTag() + ")?\n" +
                "All associated bookings and maintenance records will be removed.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (equipmentDAO.delete(eq.getEquipmentId())) {
                    auditDAO.log(currentUser.getUserId(), "EQUIPMENT_DELETED", "EQUIPMENT", eq.getEquipmentId(), "Deleted " + eq.getAssetTag());
                    JOptionPane.showMessageDialog(this, "Equipment deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting equipment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
