package com.poornima.lab.ui.dashboard;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.auth.ChangePasswordDialog;
import com.poornima.lab.ui.auth.LoginFrame;
import com.poornima.lab.ui.dialogs.BookingDialog;
import com.poornima.lab.ui.dialogs.EquipmentDialog;
import com.poornima.lab.ui.dialogs.ExportDialog;
import com.poornima.lab.ui.dialogs.MaintenanceDialog;

import javax.swing.*;
import java.awt.*;

public class MainDashboardFrame extends JFrame {
    private final User currentUser;
    private final DashboardViewPanel dashboardViewPanel;

    public MainDashboardFrame(User user) {
        super("Poornima Institute of Engineering & Technology - Lab Equipment Management System");
        this.currentUser = user;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        setJMenuBar(createAppMenuBar());

        dashboardViewPanel = new DashboardViewPanel(user, this);
        setContentPane(dashboardViewPanel);
    }

    private JMenuBar createAppMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(Color.WHITE);

        // File Menu
        JMenu menuFile = new JMenu("File");
        JMenuItem miRefresh = new JMenuItem("Refresh All Views");
        miRefresh.addActionListener(e -> dashboardViewPanel.switchView("OVERVIEW"));
        JMenuItem miExport = new JMenuItem("Export Data...");
        miExport.addActionListener(e -> new ExportDialog(this).setVisible(true));
        JMenuItem miPass = new JMenuItem("Change My Password...");
        miPass.addActionListener(e -> new ChangePasswordDialog(this, currentUser.getUsername()).setVisible(true));
        JMenuItem miLogout = new JMenuItem("Logout");
        miLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });
        JMenuItem miExit = new JMenuItem("Exit");
        miExit.addActionListener(e -> System.exit(0));

        menuFile.add(miRefresh);
        menuFile.add(miExport);
        menuFile.addSeparator();
        menuFile.add(miPass);
        menuFile.add(miLogout);
        menuFile.addSeparator();
        menuFile.add(miExit);

        // Equipment Menu
        JMenu menuEq = new JMenu("Equipment");
        JMenuItem miAddEq = new JMenuItem("Add New Asset");
        miAddEq.addActionListener(e -> new EquipmentDialog(this, null, currentUser).setVisible(true));
        JMenuItem miViewEq = new JMenuItem("Browse Inventory");
        miViewEq.addActionListener(e -> switchView("EQUIPMENT"));
        menuEq.add(miAddEq);
        menuEq.add(miViewEq);

        // Bookings Menu
        JMenu menuBk = new JMenu("Reservations");
        JMenuItem miNewBk = new JMenuItem("New Reservation");
        miNewBk.addActionListener(e -> new BookingDialog(this, currentUser, null).setVisible(true));
        JMenuItem miViewBk = new JMenuItem("View Bookings Schedule");
        miViewBk.addActionListener(e -> switchView("BOOKINGS"));
        menuBk.add(miNewBk);
        menuBk.add(miViewBk);

        // Maintenance Menu
        JMenu menuMaint = new JMenu("Maintenance");
        JMenuItem miNewMaint = new JMenuItem("Log Service Ticket");
        miNewMaint.addActionListener(e -> new MaintenanceDialog(this, null, currentUser).setVisible(true));
        JMenuItem miViewMaint = new JMenuItem("Maintenance History");
        miViewMaint.addActionListener(e -> switchView("MAINTENANCE"));
        menuMaint.add(miNewMaint);
        menuMaint.add(miViewMaint);

        // Help Menu
        JMenu menuHelp = new JMenu("Help");
        JMenuItem miAbout = new JMenuItem("About Minor Project");
        miAbout.addActionListener(e -> showAboutDialog());
        JMenuItem miDb = new JMenuItem("Database Diagnostics");
        miDb.addActionListener(e -> showDbDiagnostics());
        menuHelp.add(miAbout);
        menuHelp.add(miDb);

        mb.add(menuFile);
        mb.add(menuEq);
        mb.add(menuBk);
        mb.add(menuMaint);
        mb.add(menuHelp);

        return mb;
    }

    public void switchView(String key) {
        dashboardViewPanel.switchView(key);
    }

    private void showAboutDialog() {
        String msg = "<html><body style='width: 340px; font-family: Segoe UI, sans-serif;'>" +
                "<h3 style='color: #4F46E5; margin-bottom: 4px;'>Lab Equipment Booking & Maintenance System</h3>" +
                "<p><b>Course:</b> Minor Project (Non-Syllabus Project - NSP)</p>" +
                "<p><b>Technology Stack:</b> Java 17, Java Swing, JDBC, MySQL, SQLite, FlatLaf</p>" +
                "<p><b>Institution:</b> Poornima Institute of Engineering & Technology, Jaipur</p>" +
                "<p><b>Department:</b> Department of Computer Engineering</p>" +
                "<p><b>Academic Year:</b> 2026-27</p>" +
                "<hr style='border: 0; border-top: 1px solid #E2E8F0;'>" +
                "<p style='font-size: 11px; color: #64748B;'>Engineered for automated asset reservation, preventative maintenance scheduling, and real-time laboratory inventory tracking.</p>" +
                "</body></html>";
        JOptionPane.showMessageDialog(this, msg, "About Project", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDbDiagnostics() {
        String info = "Active Database Driver: " + DatabaseConfig.getDbType() + "\n" +
                "JDBC Connection URL : " + DatabaseConfig.getJdbcUrl() + "\n" +
                "Auto-Schema Recovery: Supported\n" +
                "Foreign Key Enforcement: Enabled\n" +
                "Current User Role: " + currentUser.getRole() + "\n" +
                "User Department: " + currentUser.getDepartment();
        JOptionPane.showMessageDialog(this, info, "Database Diagnostics", JOptionPane.INFORMATION_MESSAGE);
    }

    public DashboardViewPanel getDashboardViewPanel() {
        return dashboardViewPanel;
    }
}
