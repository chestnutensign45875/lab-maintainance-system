package com.poornima.lab.ui.auth;

import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.UserDAO;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.SecurityUtil;
import com.poornima.lab.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordPanel extends JPanel {
    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtOldPassword = new JPasswordField(18);
    private final JPasswordField txtNewPassword = new JPasswordField(18);
    private final JPasswordField txtConfirmPassword = new JPasswordField(18);

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final Window parentWindow;

    public ChangePasswordPanel(Window parentWindow, String prefilledUsername) {
        this.parentWindow = parentWindow;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Security & Password Reset");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 12));
        form.setBackground(UITheme.BG_SURFACE);
        form.setBorder(UITheme.createCardBorder());

        form.add(new JLabel("Username:"));
        form.add(txtUsername);
        if (prefilledUsername != null && !prefilledUsername.isEmpty()) {
            txtUsername.setText(prefilledUsername);
            txtUsername.setEditable(false);
        }

        form.add(new JLabel("Current Password:"));
        form.add(txtOldPassword);

        form.add(new JLabel("New Password:"));
        form.add(txtNewPassword);

        form.add(new JLabel("Confirm New Password:"));
        form.add(txtConfirmPassword);

        add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.Style.SECONDARY);
        btnCancel.addActionListener(e -> {
            if (parentWindow != null) parentWindow.dispose();
        });

        ModernButton btnSave = new ModernButton("Update Password", ModernButton.Style.PRIMARY);
        btnSave.addActionListener(e -> handleChangePassword());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void handleChangePassword() {
        String username = txtUsername.getText().trim();
        String oldPass = new String(txtOldPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (ValidationUtil.isEmpty(username) || ValidationUtil.isEmpty(oldPass) || ValidationUtil.isEmpty(newPass)) {
            JOptionPane.showMessageDialog(this, "Please enter all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidPassword(newPass)) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.findByUsername(username);
            if (user == null || !SecurityUtil.verifyPassword(oldPass, user.getPasswordHash())) {
                JOptionPane.showMessageDialog(this, "Invalid username or current password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (userDAO.updatePassword(user.getUserId(), newPass)) {
                auditDAO.log(user.getUserId(), "PASSWORD_CHANGED", "USER", user.getUserId(), "Password updated successfully");
                JOptionPane.showMessageDialog(this, "Password updated successfully! Please login with your new credentials.", "Success", JOptionPane.INFORMATION_MESSAGE);
                if (parentWindow != null) parentWindow.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
