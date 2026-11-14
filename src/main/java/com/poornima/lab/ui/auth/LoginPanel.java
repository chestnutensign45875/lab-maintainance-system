package com.poornima.lab.ui.auth;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.UserDAO;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.dashboard.MainDashboardFrame;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginPanel extends JPanel {
    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JCheckBox chkShowPassword = new JCheckBox("Show Password");
    private final JComboBox<String> cmbRole = new JComboBox<>(new String[]{"ADMIN", "FACULTY", "STUDENT", "TECHNICIAN"});

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final Window parentWindow;

    public LoginPanel(Window parentWindow) {
        this.parentWindow = parentWindow;
        setLayout(new GridLayout(1, 2));

        // Left Banner Panel
        JPanel leftBanner = createLeftBanner();
        add(leftBanner);

        // Right Form Panel
        JPanel rightForm = createRightForm();
        add(rightForm);

        // Enter key listener on password
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocusInWindow();
                }
            }
        });

        // Show/hide password toggle
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
    }

    private JPanel createLeftBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.BG_SIDEBAR, getWidth(), getHeight(), new Color(15, 23, 42));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(79, 70, 229, 40));
                g2.fillOval(-40, -40, 220, 220);
                g2.fillOval(getWidth() - 120, getHeight() - 140, 240, 240);
                g2.dispose();
            }
        };
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(BorderFactory.createEmptyBorder(40, 36, 40, 36));

        JLabel collegeName = new JLabel("POORNIMA INSTITUTE");
        collegeName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        collegeName.setForeground(new Color(165, 180, 252));

        JLabel deptName = new JLabel("DEPARTMENT OF COMPUTER ENGINEERING");
        deptName.setFont(new Font("Segoe UI", Font.BOLD, 10));
        deptName.setForeground(UITheme.TEXT_MUTED);

        JLabel projectTitle = new JLabel("<html>Lab Equipment Booking<br>&amp; Maintenance System</html>");
        projectTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        projectTitle.setForeground(Color.WHITE);

        JLabel courseTag = new JLabel("Mini Project (Non-Syllabus Project - NSP)");
        courseTag.setFont(UITheme.FONT_SMALL);
        courseTag.setForeground(new Color(199, 210, 254));

        banner.add(collegeName);
        banner.add(deptName);
        banner.add(Box.createVerticalStrut(20));
        banner.add(projectTitle);
        banner.add(Box.createVerticalStrut(8));
        banner.add(courseTag);
        banner.add(Box.createVerticalStrut(30));

        String[] features = {
                "✓ Real-time Equipment Scheduling & Conflict Avoidance",
                "✓ Comprehensive Preventive & Corrective Maintenance Logs",
                "✓ Role-Based Access Control (Admin, Faculty, Student, Tech)",
                "✓ Complete JDBC CRUD with High-Performance Database",
                "✓ Instant CSV Audit Reporting & Analytics Export"
        };
        for (String feat : features) {
            JLabel lbl = new JLabel(feat);
            lbl.setFont(UITheme.FONT_BODY);
            lbl.setForeground(UITheme.TEXT_SIDEBAR);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            banner.add(lbl);
        }

        banner.add(Box.createVerticalGlue());

        String dbInfo = "Database: " + DatabaseConfig.getDbType() + " (" +
                ("MYSQL".equals(DatabaseConfig.getDbType()) ? "Connected Live" : "Active Offline Engine") + ")";
        JLabel lblDb = new JLabel(dbInfo);
        lblDb.setFont(UITheme.FONT_SMALL);
        lblDb.setForeground(new Color(147, 197, 253));
        lblDb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246, 120), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        banner.add(lblDb);

        return banner;
    }

    private JPanel createRightForm() {
        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.setBackground(UITheme.BG_MAIN);
        formWrapper.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(UITheme.BG_SURFACE);
        formCard.setBorder(UITheme.createCardBorder());

        JLabel lblLoginHeader = new JLabel("Sign In to Portal");
        lblLoginHeader.setFont(UITheme.FONT_TITLE);
        lblLoginHeader.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Enter your credentials to access laboratory tools");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        formCard.add(lblLoginHeader);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(lblSub);
        formCard.add(Box.createVerticalStrut(20));

        JLabel lblRole = new JLabel("Target Role:");
        lblRole.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(lblRole);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(cmbRole);
        formCard.add(Box.createVerticalStrut(12));

        JLabel lblUser = new JLabel("Username / ID:");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(lblUser);
        formCard.add(Box.createVerticalStrut(4));
        txtUsername.setFont(UITheme.FONT_BODY);
        formCard.add(txtUsername);
        formCard.add(Box.createVerticalStrut(12));

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(lblPass);
        formCard.add(Box.createVerticalStrut(4));
        txtPassword.setFont(UITheme.FONT_BODY);
        formCard.add(txtPassword);
        formCard.add(Box.createVerticalStrut(6));

        chkShowPassword.setFont(UITheme.FONT_SMALL);
        chkShowPassword.setOpaque(false);
        formCard.add(chkShowPassword);
        formCard.add(Box.createVerticalStrut(16));

        ModernButton btnLogin = new ModernButton("Sign In", ModernButton.Style.PRIMARY);
        btnLogin.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        btnLogin.addActionListener(e -> handleLogin());
        formCard.add(btnLogin);
        formCard.add(Box.createVerticalStrut(10));

        JPanel linksPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        linksPanel.setOpaque(false);

        JButton btnRegister = new JButton("Register New");
        btnRegister.setFont(UITheme.FONT_SMALL);
        btnRegister.setForeground(UITheme.PRIMARY);
        btnRegister.setBorderPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> {
            if (parentWindow != null) {
                RegisterDialog dlg = new RegisterDialog((Frame) parentWindow);
                dlg.setVisible(true);
            }
        });

        JButton btnForgot = new JButton("Reset Password");
        btnForgot.setFont(UITheme.FONT_SMALL);
        btnForgot.setForeground(UITheme.TEXT_SECONDARY);
        btnForgot.setBorderPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.addActionListener(e -> {
            if (parentWindow != null) {
                ChangePasswordDialog dlg = new ChangePasswordDialog(parentWindow, txtUsername.getText().trim());
                dlg.setVisible(true);
            }
        });

        linksPanel.add(btnRegister);
        linksPanel.add(btnForgot);
        formCard.add(linksPanel);
        formCard.add(Box.createVerticalStrut(16));

        JLabel lblQuick = new JLabel("Quick Fill Demo Accounts:");
        lblQuick.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblQuick.setForeground(UITheme.TEXT_MUTED);
        formCard.add(lblQuick);
        formCard.add(Box.createVerticalStrut(6));

        JPanel demoButtons = new JPanel(new GridLayout(1, 4, 6, 0));
        demoButtons.setOpaque(false);

        demoButtons.add(createDemoButton("Admin", "admin", "admin123", "ADMIN"));
        demoButtons.add(createDemoButton("Faculty", "prof_sharma", "faculty123", "FACULTY"));
        demoButtons.add(createDemoButton("Student", "ayush_kumar", "student123", "STUDENT"));
        demoButtons.add(createDemoButton("Tech", "tech_rajesh", "tech123", "TECHNICIAN"));
        formCard.add(demoButtons);

        txtUsername.setText("admin");
        txtPassword.setText("admin123");
        cmbRole.setSelectedItem("ADMIN");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formWrapper.add(formCard, gbc);

        return formWrapper;
    }

    private JButton createDemoButton(String label, String username, String pass, String role) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btn.setMargin(new Insets(3, 4, 3, 4));
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            txtUsername.setText(username);
            txtPassword.setText(pass);
            cmbRole.setSelectedItem(role);
        });
        return btn;
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String selectedRole = (String) cmbRole.getSelectedItem();

        if (ValidationUtil.isEmpty(username) || ValidationUtil.isEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid credentials or inactive account.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedRole != null && !selectedRole.equalsIgnoreCase(user.getRole())) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "User '" + username + "' is registered as " + user.getRole() + ", but you selected " + selectedRole + ".\n" +
                        "Would you like to proceed as " + user.getRole() + "?",
                        "Role Mismatch", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            auditDAO.log(user.getUserId(), "USER_LOGIN", "AUTH", user.getUserId(), "Successful login by " + user.getUsername());

            if (parentWindow != null) {
                parentWindow.dispose();
            }
            SwingUtilities.invokeLater(() -> {
                MainDashboardFrame dashboard = new MainDashboardFrame(user);
                dashboard.setVisible(true);
            });

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
