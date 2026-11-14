package com.poornima.lab.ui.dashboard;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.auth.ChangePasswordDialog;
import com.poornima.lab.ui.auth.LoginFrame;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.dialogs.ExportDialog;
import com.poornima.lab.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DashboardViewPanel extends JPanel {
    private final User currentUser;
    private final Window parentWindow;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);

    private final OverviewPanel overviewPanel;
    private final EquipmentPanel equipmentPanel;
    private final BookingPanel bookingPanel;
    private final MaintenancePanel maintenancePanel;
    private final UserManagementPanel userPanel;
    private final ReportsPanel reportsPanel;

    private final Map<String, JButton> navButtons = new HashMap<>();
    private String activeNav = "OVERVIEW";
    private final JLabel lblClock = new JLabel();

    public DashboardViewPanel(User user, Window parentWindow) {
        this.currentUser = user;
        this.parentWindow = parentWindow;
        setLayout(new BorderLayout());

        // Top Header
        JPanel topHeader = createTopHeader();
        add(topHeader, BorderLayout.NORTH);

        // Left Navigation Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Center Content Area (Cards)
        overviewPanel = new OverviewPanel(currentUser, null);
        equipmentPanel = new EquipmentPanel(currentUser);
        bookingPanel = new BookingPanel(currentUser);
        maintenancePanel = new MaintenancePanel(currentUser);
        userPanel = new UserManagementPanel(currentUser);
        reportsPanel = new ReportsPanel();

        contentArea.add(overviewPanel, "OVERVIEW");
        contentArea.add(equipmentPanel, "EQUIPMENT");
        contentArea.add(bookingPanel, "BOOKINGS");
        contentArea.add(maintenancePanel, "MAINTENANCE");
        if (currentUser.isAdmin()) {
            contentArea.add(userPanel, "USERS");
        }
        contentArea.add(reportsPanel, "REPORTS");

        add(contentArea, BorderLayout.CENTER);

        updateClock();
        try {
            Timer timer = new Timer(1000, e -> updateClock());
            timer.start();
        } catch (Exception ignored) {}
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("⚡");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(UITheme.PRIMARY);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);

        JLabel mainTitle = new JLabel("POORNIMA INSTITUTE OF ENGINEERING & TECHNOLOGY");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subTitle = new JLabel("Lab Equipment Booking & Maintenance System | Dept of Computer Engineering");
        subTitle.setFont(UITheme.FONT_SMALL);
        subTitle.setForeground(UITheme.TEXT_SECONDARY);

        titles.add(mainTitle);
        titles.add(subTitle);
        left.add(logo);
        left.add(titles);

        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        lblClock.setFont(UITheme.FONT_SMALL);
        lblClock.setForeground(UITheme.TEXT_SECONDARY);

        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        userBadge.setBackground(UITheme.PRIMARY_LIGHT);
        userBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(199, 210, 254), 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        JLabel lblUser = new JLabel(currentUser.getFullName() + "  [" + currentUser.getRole() + "]");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(UITheme.PRIMARY);
        userBadge.add(lblUser);

        ModernButton btnLogout = new ModernButton("Logout", ModernButton.Style.SECONDARY);
        btnLogout.addActionListener(e -> handleLogout());

        right.add(lblClock);
        right.add(userBadge);
        right.add(btnLogout);

        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

        JLabel navHeader = new JLabel("NAVIGATION");
        navHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        navHeader.setForeground(new Color(148, 163, 184));
        navHeader.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        sidebar.add(navHeader);

        addNavButton(sidebar, "OVERVIEW", "📊  Dashboard Overview");
        addNavButton(sidebar, "EQUIPMENT", "🔬  Equipment Catalog");
        addNavButton(sidebar, "BOOKINGS", "📅  Reservations Schedule");
        addNavButton(sidebar, "MAINTENANCE", "🛠️  Maintenance & Repair");

        if (currentUser.isAdmin()) {
            addNavButton(sidebar, "USERS", "👥  User Accounts");
        }

        addNavButton(sidebar, "REPORTS", "📑  Reports & Analytics");

        sidebar.add(Box.createVerticalGlue());

        JPanel dbBox = new JPanel(new GridLayout(2, 1, 0, 2));
        dbBox.setBackground(new Color(15, 23, 42));
        dbBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel dbTitle = new JLabel("DATABASE ENGINE");
        dbTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        dbTitle.setForeground(new Color(148, 163, 184));

        JLabel dbVal = new JLabel("● " + DatabaseConfig.getDbType() + " Active");
        dbVal.setFont(UITheme.FONT_SMALL);
        dbVal.setForeground(new Color(52, 211, 153));

        dbBox.add(dbTitle);
        dbBox.add(dbVal);
        sidebar.add(dbBox);

        updateNavStyles();
        return sidebar;
    }

    private void addNavButton(JPanel sidebar, String key, String title) {
        JButton btn = new JButton(title);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(10, 14, 10, 14));

        btn.addActionListener(e -> switchView(key));

        navButtons.put(key, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(6));
    }

    public void switchView(String key) {
        activeNav = key;
        cardLayout.show(contentArea, key);
        updateNavStyles();

        switch (key) {
            case "OVERVIEW": overviewPanel.refreshData(); break;
            case "EQUIPMENT": equipmentPanel.loadData(); break;
            case "BOOKINGS": bookingPanel.loadData(); break;
            case "MAINTENANCE": maintenancePanel.loadData(); break;
            case "USERS": if (userPanel != null) userPanel.loadData(); break;
            case "REPORTS": reportsPanel.loadData(); break;
        }
    }

    private void updateNavStyles() {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(activeNav)) {
                btn.setBackground(UITheme.SIDEBAR_SELECTED);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(UITheme.BG_SIDEBAR);
                btn.setForeground(UITheme.TEXT_SIDEBAR);
            }
        }
    }

    private void updateClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy | hh:mm:ss a");
        lblClock.setText(sdf.format(new Date()));
    }

    private void handleLogout() {
        if (parentWindow != null) {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                new AuditLogDAO().log(currentUser.getUserId(), "USER_LOGOUT", "AUTH", currentUser.getUserId(), "User logged out");
                parentWindow.dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        }
    }

    public OverviewPanel getOverviewPanel() { return overviewPanel; }
    public EquipmentPanel getEquipmentPanel() { return equipmentPanel; }
    public BookingPanel getBookingPanel() { return bookingPanel; }
    public MaintenancePanel getMaintenancePanel() { return maintenancePanel; }
    public UserManagementPanel getUserPanel() { return userPanel; }
    public ReportsPanel getReportsPanel() { return reportsPanel; }
}
