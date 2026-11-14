package com.poornima.lab.ui.auth;

import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.UserDAO;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {
    private final JTextField txtUsername = new JTextField(20);
    private final JTextField txtFullName = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtPhone = new JTextField(20);
    private final JComboBox<String> cmbRole = new JComboBox<>(new String[]{"STUDENT", "FACULTY", "TECHNICIAN"});
    private final JComboBox<String> cmbDept = new JComboBox<>(new String[]{
            "Computer Engineering", "AI & Data Science", "Information Technology",
            "Electronics & Comm.", "Mechanical Engineering", "Civil Engineering"
    });
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JPasswordField txtConfirmPassword = new JPasswordField(20);

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final Window parentWindow;
    private boolean registered = false;

    public RegisterPanel(Window parentWindow) {
        this.parentWindow = parentWindow;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Create Your Lab Account");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.PRIMARY);

        JLabel sub = new JLabel("Enter your academic details to reserve and manage laboratory equipment");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(headerPanel, BorderLayout.NORTH);

        // Form Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.BG_SURFACE);
        card.setBorder(UITheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 1.0;

        int row = 0;
        addFormField(card, gbc, row++, "Full Name:", txtFullName);
        addFormField(card, gbc, row++, "Username (Min 3 chars):", txtUsername);
        addFormField(card, gbc, row++, "Official Email ID:", txtEmail);
        addFormField(card, gbc, row++, "Mobile Number:", txtPhone);
        addFormField(card, gbc, row++, "Department:", cmbDept);
        addFormField(card, gbc, row++, "User Role:", cmbRole);
        addFormField(card, gbc, row++, "Password (Min 6 chars):", txtPassword);
        addFormField(card, gbc, row++, "Confirm Password:", txtConfirmPassword);

        add(card, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.Style.SECONDARY);
        btnCancel.addActionListener(e -> {
            if (parentWindow != null) parentWindow.dispose();
        });

        ModernButton btnSubmit = new ModernButton("Register Account", ModernButton.Style.PRIMARY);
        btnSubmit.addActionListener(e -> handleRegistration());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSubmit);
        add(buttonPanel, BorderLayout.SOUTH);
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

    private void handleRegistration() {
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        String dept = (String) cmbDept.getSelectedItem();
        String pass = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (ValidationUtil.isEmpty(fullName) || ValidationUtil.isEmpty(username) || ValidationUtil.isEmpty(email)) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidUsername(username)) {
            JOptionPane.showMessageDialog(this, "Username must be 3-30 characters long (letters, numbers, underscore).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidPassword(pass)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match. Please re-enter.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.findByUsername(username) != null) {
                JOptionPane.showMessageDialog(this, "Username '" + username + "' is already taken. Please choose another.", "Registration Conflict", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = new User(0, username, pass, fullName, email, role, dept, phone, "ACTIVE");
            if (userDAO.create(user)) {
                auditDAO.log(user.getUserId(), "USER_REGISTERED", "USER", user.getUserId(), "New account created: " + username);
                JOptionPane.showMessageDialog(this, "Account registered successfully! You can now log in.", "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                registered = true;
                if (parentWindow != null) parentWindow.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register account. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistered() {
        return registered;
    }
}
