package com.poornima.lab.ui.dashboard;

import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.UserDAO;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.auth.RegisterDialog;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.components.ModernTable;
import com.poornima.lab.ui.components.StatusBadgeRenderer;
import com.poornima.lab.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private final JTextField txtSearch = new JTextField(18);
    private final DefaultTableModel tableModel;
    private final ModernTable table;

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    private final User currentUser;
    private List<User> currentList;

    public UserManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Search Bar Card
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(UITheme.BG_SURFACE);
        filterCard.setBorder(UITheme.createCardBorder());

        JLabel lblSearch = new JLabel("🔍 Search Users:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(lblSearch);
        txtSearch.setToolTipText("Search by username, full name, email, or department...");
        filterCard.add(txtSearch);

        ModernButton btnReset = new ModernButton("Reset", ModernButton.Style.SECONDARY);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });
        filterCard.add(btnReset);

        add(filterCard, BorderLayout.NORTH);

        // 2. Table Card
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(UITheme.BG_SURFACE);
        tableCard.setBorder(UITheme.createCardBorder());

        String[] cols = {"User ID", "Username", "Full Name", "Official Email", "Role", "Department", "Phone", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new ModernTable(tableModel);

        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionPanel.setOpaque(false);

        ModernButton btnAdd = new ModernButton("+ Add New User", ModernButton.Style.PRIMARY);
        btnAdd.addActionListener(e -> {
            RegisterDialog dlg = new RegisterDialog((Frame) SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            if (dlg.isRegistered()) loadData();
        });

        ModernButton btnToggleStatus = new ModernButton("Toggle Active / Blocked", ModernButton.Style.SECONDARY);
        btnToggleStatus.addActionListener(e -> handleToggleStatus());

        ModernButton btnChangeRole = new ModernButton("Change User Role", ModernButton.Style.SECONDARY);
        btnChangeRole.addActionListener(e -> handleChangeRole());

        ModernButton btnResetPass = new ModernButton("Reset Password", ModernButton.Style.SECONDARY);
        btnResetPass.addActionListener(e -> handleResetPassword());

        ModernButton btnDelete = new ModernButton("Delete Account", ModernButton.Style.DANGER);
        btnDelete.addActionListener(e -> handleDeleteUser());

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.Style.SECONDARY);
        btnRefresh.addActionListener(e -> loadData());

        actionPanel.add(btnAdd);
        actionPanel.add(btnToggleStatus);
        actionPanel.add(btnChangeRole);
        actionPanel.add(btnResetPass);
        actionPanel.add(btnDelete);
        actionPanel.add(btnRefresh);

        tableCard.add(actionPanel, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });

        loadData();
    }

    public void loadData() {
        try {
            String kw = txtSearch.getText().trim();
            currentList = kw.isEmpty() ? userDAO.getAllUsers() : userDAO.searchUsers(kw);
            tableModel.setRowCount(0);

            for (User u : currentList) {
                tableModel.addRow(new Object[]{
                        u.getUserId(),
                        u.getUsername(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getRole(),
                        u.getDepartment(),
                        u.getPhone() != null ? u.getPhone() : "-",
                        u.getStatus()
                });
            }
        } catch (Exception ex) {
            System.err.println("Error loading users: " + ex.getMessage());
        }
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1 || currentList == null || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void handleToggleStatus() {
        User u = getSelectedUser();
        if (u == null) return;

        if (u.getUserId() == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot block your own active session.", "Operation Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newStatus = "ACTIVE".equalsIgnoreCase(u.getStatus()) ? "BLOCKED" : "ACTIVE";
        u.setStatus(newStatus);
        try {
            if (userDAO.update(u)) {
                auditDAO.log(currentUser.getUserId(), "USER_STATUS_CHANGED", "USER", u.getUserId(), "Set status to " + newStatus);
                JOptionPane.showMessageDialog(this, "User status updated to " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleChangeRole() {
        User u = getSelectedUser();
        if (u == null) return;

        String[] roles = {"ADMIN", "FACULTY", "STUDENT", "TECHNICIAN"};
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select new role for user '" + u.getFullName() + "':",
                "Change Role", JOptionPane.QUESTION_MESSAGE, null, roles, u.getRole());

        if (selected != null && !selected.equals(u.getRole())) {
            u.setRole(selected);
            try {
                if (userDAO.update(u)) {
                    auditDAO.log(currentUser.getUserId(), "USER_ROLE_CHANGED", "USER", u.getUserId(), "Role changed to " + selected);
                    JOptionPane.showMessageDialog(this, "User role updated successfully to " + selected, "Role Updated", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating role: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleResetPassword() {
        User u = getSelectedUser();
        if (u == null) return;

        int choice = JOptionPane.showConfirmDialog(this,
                "Reset password for user '" + u.getUsername() + "' to default 'password123'?",
                "Confirm Password Reset", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (userDAO.updatePassword(u.getUserId(), "password123")) {
                    auditDAO.log(currentUser.getUserId(), "PASSWORD_RESET", "USER", u.getUserId(), "Password reset for " + u.getUsername());
                    JOptionPane.showMessageDialog(this, "Password reset successfully to 'password123'.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error resetting password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteUser() {
        User u = getSelectedUser();
        if (u == null) return;

        if (u.getUserId() == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own admin account.", "Operation Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete user '" + u.getUsername() + "'?\n" +
                "All their bookings and records will be deleted.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (userDAO.delete(u.getUserId())) {
                    auditDAO.log(currentUser.getUserId(), "USER_DELETED", "USER", u.getUserId(), "Deleted user " + u.getUsername());
                    JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
