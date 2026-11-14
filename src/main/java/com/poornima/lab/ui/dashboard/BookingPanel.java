package com.poornima.lab.ui.dashboard;

import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.BookingDAO;
import com.poornima.lab.dao.EquipmentDAO;
import com.poornima.lab.model.Booking;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.components.ModernTable;
import com.poornima.lab.ui.components.StatusBadgeRenderer;
import com.poornima.lab.ui.dialogs.BookingDialog;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.DateUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookingPanel extends JPanel {
    private final JTextField txtSearch = new JTextField(18);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
            "ALL", "ACTIVE", "APPROVED", "PENDING", "COMPLETED", "CANCELLED", "REJECTED"
    });

    private final DefaultTableModel tableModel;
    private final ModernTable table;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    private final User currentUser;
    private List<Booking> currentList;

    public BookingPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Filters & Search
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(UITheme.BG_SURFACE);
        filterCard.setBorder(UITheme.createCardBorder());

        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(lblSearch);
        txtSearch.setToolTipText("Search by asset tag, equipment name, student/faculty name, or purpose...");
        filterCard.add(txtSearch);

        JLabel lblStatus = new JLabel("Filter Status:");
        lblStatus.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(lblStatus);
        filterCard.add(cmbStatus);

        ModernButton btnReset = new ModernButton("Reset", ModernButton.Style.SECONDARY);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatus.setSelectedIndex(0);
            loadData();
        });
        filterCard.add(btnReset);

        add(filterCard, BorderLayout.NORTH);

        // 2. Center Table
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(UITheme.BG_SURFACE);
        tableCard.setBorder(UITheme.createCardBorder());

        String[] cols = {"Booking ID", "Asset Tag", "Equipment Name", "Reserved By", "Role", "Start Window", "End Window", "Status", "Purpose"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new ModernTable(tableModel);

        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(95);
        table.getColumnModel().getColumn(2).setPreferredWidth(190);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons at Bottom
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionPanel.setOpaque(false);

        ModernButton btnNew = new ModernButton("+ New Reservation", ModernButton.Style.PRIMARY);
        btnNew.addActionListener(e -> handleNewBooking());
        actionPanel.add(btnNew);

        ModernButton btnReturn = new ModernButton("Return Equipment / Complete", ModernButton.Style.SUCCESS);
        btnReturn.addActionListener(e -> handleReturn());
        actionPanel.add(btnReturn);

        if (currentUser.isAdmin() || currentUser.isFaculty() || currentUser.isTechnician()) {
            ModernButton btnApprove = new ModernButton("Approve Reservation", ModernButton.Style.SECONDARY);
            btnApprove.addActionListener(e -> handleApprove());
            actionPanel.add(btnApprove);
        }

        ModernButton btnCancel = new ModernButton("Cancel Reservation", ModernButton.Style.DANGER);
        btnCancel.addActionListener(e -> handleCancel());
        actionPanel.add(btnCancel);

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.Style.SECONDARY);
        btnRefresh.addActionListener(e -> loadData());
        actionPanel.add(btnRefresh);

        tableCard.add(actionPanel, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        // Search listeners
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });
        cmbStatus.addActionListener(e -> loadData());

        loadData();
    }

    public void loadData() {
        try {
            String kw = txtSearch.getText().trim();
            String status = (String) cmbStatus.getSelectedItem();

            currentList = bookingDAO.searchBookings(kw, status);
            tableModel.setRowCount(0);

            for (Booking b : currentList) {
                tableModel.addRow(new Object[]{
                        b.getBookingId(),
                        b.getAssetTag(),
                        b.getEquipmentName(),
                        b.getUserName(),
                        b.getUserRole(),
                        DateUtil.formatDateTime(b.getStartTime()),
                        DateUtil.formatDateTime(b.getEndTime()),
                        b.getStatus(),
                        b.getPurpose()
                });
            }
        } catch (Exception ex) {
            System.err.println("Error loading bookings data: " + ex.getMessage());
        }
    }

    private Booking getSelectedBooking() {
        int row = table.getSelectedRow();
        if (row == -1 || currentList == null || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentList.get(row);
    }

    private void handleNewBooking() {
        BookingDialog dlg = new BookingDialog(SwingUtilities.getWindowAncestor(this), currentUser, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void handleReturn() {
        Booking b = getSelectedBooking();
        if (b == null) return;

        if ("COMPLETED".equalsIgnoreCase(b.getStatus())) {
            JOptionPane.showMessageDialog(this, "This booking is already marked as COMPLETED.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Confirm physical check-in and return of equipment '" + b.getEquipmentName() + "' (" + b.getAssetTag() + ")?\n" +
                "Status will be updated to COMPLETED and the equipment will become AVAILABLE for other users.",
                "Equipment Return Verification", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (bookingDAO.updateStatus(b.getBookingId(), "COMPLETED", "Returned and verified in good condition")) {
                    equipmentDAO.updateStatus(b.getEquipmentId(), "AVAILABLE");
                    auditDAO.log(currentUser.getUserId(), "BOOKING_RETURNED", "BOOKING", b.getBookingId(), "Returned equipment " + b.getAssetTag());
                    JOptionPane.showMessageDialog(this, "Equipment returned and checked back into lab inventory!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error completing booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleApprove() {
        Booking b = getSelectedBooking();
        if (b == null) return;

        if (!"PENDING".equalsIgnoreCase(b.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only PENDING bookings can be approved.", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (bookingDAO.updateStatus(b.getBookingId(), "APPROVED", "Approved by " + currentUser.getFullName())) {
                equipmentDAO.updateStatus(b.getEquipmentId(), "BOOKED");
                auditDAO.log(currentUser.getUserId(), "BOOKING_APPROVED", "BOOKING", b.getBookingId(), "Approved booking #" + b.getBookingId());
                JOptionPane.showMessageDialog(this, "Booking approved successfully!", "Approved", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error approving booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancel() {
        Booking b = getSelectedBooking();
        if (b == null) return;

        if ("CANCELLED".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot cancel a booking that is already " + b.getStatus() + ".", "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel booking #" + b.getBookingId() + " for '" + b.getEquipmentName() + "'?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (bookingDAO.updateStatus(b.getBookingId(), "CANCELLED", "Cancelled by " + currentUser.getFullName())) {
                    equipmentDAO.updateStatus(b.getEquipmentId(), "AVAILABLE");
                    auditDAO.log(currentUser.getUserId(), "BOOKING_CANCELLED", "BOOKING", b.getBookingId(), "Cancelled booking #" + b.getBookingId());
                    JOptionPane.showMessageDialog(this, "Booking cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
