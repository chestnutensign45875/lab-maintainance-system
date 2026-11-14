package com.poornima.lab.ui.dashboard;

import com.poornima.lab.dao.BookingDAO;
import com.poornima.lab.dao.EquipmentDAO;
import com.poornima.lab.dao.MaintenanceDAO;
import com.poornima.lab.model.Booking;
import com.poornima.lab.model.MaintenanceRecord;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.components.ModernTable;
import com.poornima.lab.ui.components.StatCard;
import com.poornima.lab.ui.components.StatusBadgeRenderer;
import com.poornima.lab.ui.dialogs.BookingDialog;
import com.poornima.lab.ui.dialogs.EquipmentDialog;
import com.poornima.lab.ui.dialogs.ExportDialog;
import com.poornima.lab.ui.dialogs.MaintenanceDialog;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OverviewPanel extends JPanel {
    private final StatCard cardTotal;
    private final StatCard cardAvailable;
    private final StatCard cardMaintenance;
    private final StatCard cardBookings;

    private final DefaultTableModel recentBookingsModel;
    private final ModernTable recentBookingsTable;

    private final DefaultTableModel recentMaintModel;
    private final ModernTable recentMaintTable;

    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();

    private final User currentUser;
    private final MainDashboardFrame parentFrame;

    public OverviewPanel(User currentUser, MainDashboardFrame parentFrame) {
        this.currentUser = currentUser;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top KPI Stat Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setOpaque(false);

        cardTotal = new StatCard("Total Equipment", "0", "Cataloged lab assets", UITheme.PRIMARY);
        cardAvailable = new StatCard("Available Now", "0", "Ready for checkout", UITheme.SUCCESS_TEXT);
        cardMaintenance = new StatCard("In Maintenance", "0", "Under repair or calibration", UITheme.DANGER_TEXT);
        cardBookings = new StatCard("Active Reservations", "0", "Currently scheduled", UITheme.INFO_TEXT);

        statsGrid.add(cardTotal);
        statsGrid.add(cardAvailable);
        statsGrid.add(cardMaintenance);
        statsGrid.add(cardBookings);

        add(statsGrid, BorderLayout.NORTH);

        // 2. Middle Quick Actions Bar
        JPanel quickBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        quickBar.setOpaque(false);

        ModernButton btnBook = new ModernButton("+ Reserve Equipment", ModernButton.Style.PRIMARY);
        btnBook.addActionListener(e -> {
            BookingDialog dlg = new BookingDialog(SwingUtilities.getWindowAncestor(this), currentUser, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) refreshData();
        });

        ModernButton btnAddEq = new ModernButton("+ Add New Asset", ModernButton.Style.SECONDARY);
        btnAddEq.addActionListener(e -> {
            EquipmentDialog dlg = new EquipmentDialog(SwingUtilities.getWindowAncestor(this), null, currentUser);
            dlg.setVisible(true);
            if (dlg.isSaved()) refreshData();
        });

        ModernButton btnMaint = new ModernButton("+ Log Maintenance", ModernButton.Style.SECONDARY);
        btnMaint.addActionListener(e -> {
            MaintenanceDialog dlg = new MaintenanceDialog(SwingUtilities.getWindowAncestor(this), null, currentUser);
            dlg.setVisible(true);
            if (dlg.isSaved()) refreshData();
        });

        ModernButton btnReport = new ModernButton("Export Reports", ModernButton.Style.SECONDARY);
        btnReport.addActionListener(e -> {
            ExportDialog dlg = new ExportDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
        });

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.Style.SECONDARY);
        btnRefresh.addActionListener(e -> refreshData());

        quickBar.add(btnBook);
        if (currentUser.isAdmin() || currentUser.isTechnician()) {
            quickBar.add(btnAddEq);
            quickBar.add(btnMaint);
        }
        quickBar.add(btnReport);
        quickBar.add(btnRefresh);

        // 3. Center Split: Recent Bookings & Maintenance Tickets
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesPanel.setOpaque(false);

        // Left Card: Recent Bookings
        JPanel bookingsCard = new JPanel(new BorderLayout(0, 10));
        bookingsCard.setBackground(UITheme.BG_SURFACE);
        bookingsCard.setBorder(UITheme.createCardBorder());

        JLabel lblBTitle = new JLabel("Recent Equipment Bookings");
        lblBTitle.setFont(UITheme.FONT_SUBTITLE);
        lblBTitle.setForeground(UITheme.TEXT_PRIMARY);
        bookingsCard.add(lblBTitle, BorderLayout.NORTH);

        recentBookingsModel = new DefaultTableModel(new String[]{"ID", "Asset Tag", "User", "Date", "Status"}, 0);
        recentBookingsTable = new ModernTable(recentBookingsModel);
        recentBookingsTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
        recentBookingsTable.getColumnModel().getColumn(0).setMaxWidth(45);
        recentBookingsTable.getColumnModel().getColumn(1).setPreferredWidth(90);

        JScrollPane scrollB = new JScrollPane(recentBookingsTable);
        bookingsCard.add(scrollB, BorderLayout.CENTER);

        // Right Card: Recent Maintenance
        JPanel maintCard = new JPanel(new BorderLayout(0, 10));
        maintCard.setBackground(UITheme.BG_SURFACE);
        maintCard.setBorder(UITheme.createCardBorder());

        JLabel lblMTitle = new JLabel("Active & Recent Maintenance Tickets");
        lblMTitle.setFont(UITheme.FONT_SUBTITLE);
        lblMTitle.setForeground(UITheme.TEXT_PRIMARY);
        maintCard.add(lblMTitle, BorderLayout.NORTH);

        recentMaintModel = new DefaultTableModel(new String[]{"ID", "Asset Tag", "Type", "Cost (₹)", "Status"}, 0);
        recentMaintTable = new ModernTable(recentMaintModel);
        recentMaintTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
        recentMaintTable.getColumnModel().getColumn(0).setMaxWidth(45);
        recentMaintTable.getColumnModel().getColumn(1).setPreferredWidth(90);

        JScrollPane scrollM = new JScrollPane(recentMaintTable);
        maintCard.add(scrollM, BorderLayout.CENTER);

        tablesPanel.add(bookingsCard);
        tablesPanel.add(maintCard);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 14));
        centerWrapper.setOpaque(false);
        centerWrapper.add(quickBar, BorderLayout.NORTH);
        centerWrapper.add(tablesPanel, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        try {
            int total = equipmentDAO.getTotalCount();
            int avail = equipmentDAO.getAvailableCount();
            int maint = equipmentDAO.getUnderMaintenanceCount();
            int activeBk = bookingDAO.getActiveBookingCount();

            cardTotal.setValue(String.valueOf(total));
            cardAvailable.setValue(String.valueOf(avail));
            cardMaintenance.setValue(String.valueOf(maint));
            cardBookings.setValue(String.valueOf(activeBk));

            // Load recent bookings
            recentBookingsModel.setRowCount(0);
            List<Booking> bookings = bookingDAO.getAllBookings();
            for (int i = 0; i < Math.min(8, bookings.size()); i++) {
                Booking b = bookings.get(i);
                recentBookingsModel.addRow(new Object[]{
                        b.getBookingId(),
                        b.getAssetTag(),
                        b.getUserName(),
                        DateUtil.formatDateTime(b.getStartTime()),
                        b.getStatus()
                });
            }

            // Load recent maintenance
            recentMaintModel.setRowCount(0);
            List<MaintenanceRecord> records = maintenanceDAO.getAllRecords();
            for (int i = 0; i < Math.min(8, records.size()); i++) {
                MaintenanceRecord r = records.get(i);
                recentMaintModel.addRow(new Object[]{
                        r.getRecordId(),
                        r.getAssetTag(),
                        r.getMaintenanceType(),
                        r.getCost() != null ? String.format("%.2f", r.getCost().doubleValue()) : "0.00",
                        r.getStatus()
                });
            }
        } catch (Exception ex) {
            System.err.println("Error refreshing overview metrics: " + ex.getMessage());
        }
    }
}
