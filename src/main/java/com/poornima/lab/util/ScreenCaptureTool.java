package com.poornima.lab.util;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.dao.UserDAO;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.auth.ChangePasswordPanel;
import com.poornima.lab.ui.auth.LoginPanel;
import com.poornima.lab.ui.auth.RegisterPanel;
import com.poornima.lab.ui.dashboard.DashboardViewPanel;
import com.poornima.lab.ui.dialogs.BookingFormPanel;
import com.poornima.lab.ui.dialogs.EquipmentFormPanel;
import com.poornima.lab.ui.dialogs.ExportPanel;
import com.poornima.lab.ui.dialogs.MaintenanceFormPanel;
import com.poornima.lab.ui.theme.UITheme;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ScreenCaptureTool {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("java.awt.headless", "true");

        UITheme.setupFlatLaf();
        DatabaseConfig.initialize();

        File outDir = new File("screenshots");
        if (!outDir.exists()) outDir.mkdirs();

        try {
            UserDAO userDAO = new UserDAO();
            User admin = userDAO.findByUsername("admin");
            if (admin == null) {
                admin = new User(1, "admin", "admin123", "Prof. Anil Gupta", "admin@poornima.edu.in", "ADMIN", "Computer Engineering", "+91 9829012345", "ACTIVE");
            }

            System.out.println("Capturing Figure 5.1: Login Page...");
            captureLoginScreen(outDir);

            System.out.println("Capturing Figure 5.2: Registration Dialog...");
            captureRegistrationScreen(outDir);

            System.out.println("Capturing Figure 5.3: Main Dashboard Overview...");
            captureDashboardOverview(outDir, admin);

            System.out.println("Capturing Figure 5.4: Equipment Inventory Panel...");
            captureEquipmentPanel(outDir, admin);

            System.out.println("Capturing Figure 5.5: Add/Edit Equipment Dialog...");
            captureEquipmentDialog(outDir, admin);

            System.out.println("Capturing Figure 5.6: Booking & Reservations Panel...");
            captureBookingPanel(outDir, admin);

            System.out.println("Capturing Figure 5.7: Equipment Reservation Dialog...");
            captureBookingDialog(outDir, admin);

            System.out.println("Capturing Figure 5.8: Maintenance & Repair Panel...");
            captureMaintenancePanel(outDir, admin);

            System.out.println("Capturing Figure 5.9: Maintenance Service Ticket Dialog...");
            captureMaintenanceDialog(outDir, admin);

            System.out.println("Capturing Figure 5.10: User Management Panel...");
            captureUserManagementPanel(outDir, admin);

            System.out.println("Capturing Figure 5.11: Export & Audit Reports Dialog...");
            captureReportsDialog(outDir);

            System.out.println("Capturing Figure 5.12: Change Password Dialog...");
            captureChangePasswordDialog(outDir);

            System.out.println("All screenshots successfully captured in: " + outDir.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void captureLoginScreen(File outDir) throws Exception {
        LoginPanel panel = new LoginPanel(null);
        renderComponent(panel, 880, 560, new File(outDir, "figure_5_1_login.png"));
    }

    private static void captureRegistrationScreen(File outDir) throws Exception {
        RegisterPanel panel = new RegisterPanel(null);
        renderComponent(panel, 480, 560, new File(outDir, "figure_5_2_registration.png"));
    }

    private static void captureDashboardOverview(File outDir, User user) throws Exception {
        DashboardViewPanel panel = new DashboardViewPanel(user, null);
        panel.switchView("OVERVIEW");
        renderComponent(panel, 1240, 780, new File(outDir, "figure_5_3_dashboard.png"));
    }

    private static void captureEquipmentPanel(File outDir, User user) throws Exception {
        DashboardViewPanel panel = new DashboardViewPanel(user, null);
        panel.switchView("EQUIPMENT");
        renderComponent(panel, 1240, 780, new File(outDir, "figure_5_4_equipment_catalog.png"));
    }

    private static void captureEquipmentDialog(File outDir, User user) throws Exception {
        EquipmentFormPanel panel = new EquipmentFormPanel(null, null, user);
        renderComponent(panel, 520, 620, new File(outDir, "figure_5_5_equipment_dialog.png"));
    }

    private static void captureBookingPanel(File outDir, User user) throws Exception {
        DashboardViewPanel panel = new DashboardViewPanel(user, null);
        panel.switchView("BOOKINGS");
        renderComponent(panel, 1240, 780, new File(outDir, "figure_5_6_bookings.png"));
    }

    private static void captureBookingDialog(File outDir, User user) throws Exception {
        BookingFormPanel panel = new BookingFormPanel(null, user, null);
        renderComponent(panel, 520, 560, new File(outDir, "figure_5_7_booking_dialog.png"));
    }

    private static void captureMaintenancePanel(File outDir, User user) throws Exception {
        DashboardViewPanel panel = new DashboardViewPanel(user, null);
        panel.switchView("MAINTENANCE");
        renderComponent(panel, 1240, 780, new File(outDir, "figure_5_8_maintenance.png"));
    }

    private static void captureMaintenanceDialog(File outDir, User user) throws Exception {
        MaintenanceFormPanel panel = new MaintenanceFormPanel(null, null, user);
        renderComponent(panel, 520, 580, new File(outDir, "figure_5_9_maintenance_dialog.png"));
    }

    private static void captureUserManagementPanel(File outDir, User user) throws Exception {
        DashboardViewPanel panel = new DashboardViewPanel(user, null);
        panel.switchView("USERS");
        renderComponent(panel, 1240, 780, new File(outDir, "figure_5_10_users.png"));
    }

    private static void captureReportsDialog(File outDir) throws Exception {
        ExportPanel panel = new ExportPanel(null);
        renderComponent(panel, 780, 580, new File(outDir, "figure_5_11_reports.png"));
    }

    private static void captureChangePasswordDialog(File outDir) throws Exception {
        ChangePasswordPanel panel = new ChangePasswordPanel(null, "admin");
        renderComponent(panel, 420, 380, new File(outDir, "figure_5_12_change_password.png"));
    }

    public static void renderComponent(Container comp, int width, int height, File outFile) throws Exception {
        comp.setSize(width, height);
        layoutTree(comp);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2.setColor(comp.getBackground() != null ? comp.getBackground() : Color.WHITE);
        g2.fillRect(0, 0, width, height);

        comp.paint(g2);
        g2.dispose();

        ImageIO.write(image, "png", outFile);
    }

    private static void layoutTree(Component comp) {
        comp.doLayout();
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                layoutTree(child);
            }
        }
    }
}
