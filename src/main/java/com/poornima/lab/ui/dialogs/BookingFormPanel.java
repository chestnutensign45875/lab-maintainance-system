package com.poornima.lab.ui.dialogs;

import com.poornima.lab.dao.AuditLogDAO;
import com.poornima.lab.dao.BookingDAO;
import com.poornima.lab.dao.EquipmentDAO;
import com.poornima.lab.dao.UserDAO;
import com.poornima.lab.model.Booking;
import com.poornima.lab.model.Equipment;
import com.poornima.lab.model.User;
import com.poornima.lab.ui.components.ModernButton;
import com.poornima.lab.ui.theme.UITheme;
import com.poornima.lab.util.DateUtil;
import com.poornima.lab.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

public class BookingFormPanel extends JPanel {
    private final JComboBox<Equipment> cmbEquipment = new JComboBox<>();
    private final JComboBox<User> cmbUser = new JComboBox<>();
    private final JTextField txtStartTime = new JTextField(18);
    private final JTextField txtEndTime = new JTextField(18);
    private final JTextArea txtPurpose = new JTextArea(3, 18);
    private final JTextField txtRemarks = new JTextField(18);

    private final BookingDAO bookingDAO = new BookingDAO();
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    private final User currentUser;
    private final Equipment preselectedEquipment;
    private final Window parentWindow;
    private boolean saved = false;

    public BookingFormPanel(Window parentWindow, User currentUser, Equipment preselectedEquipment) {
        this.parentWindow = parentWindow;
        this.currentUser = currentUser;
        this.preselectedEquipment = preselectedEquipment;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        loadEquipment();
        loadUsers();

        JLabel title = new JLabel("Schedule Equipment Reservation");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UITheme.BG_SURFACE);
        formCard.setBorder(UITheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 1.0;

        int row = 0;
        addFormField(formCard, gbc, row++, "Select Equipment:", cmbEquipment);
        addFormField(formCard, gbc, row++, "Reserved By:", cmbUser);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Timestamp startDefault = new Timestamp(cal.getTimeInMillis());
        cal.add(Calendar.HOUR_OF_DAY, 3);
        Timestamp endDefault = new Timestamp(cal.getTimeInMillis());

        txtStartTime.setText(DateUtil.formatStandardDateTime(startDefault));
        txtEndTime.setText(DateUtil.formatStandardDateTime(endDefault));

        addFormField(formCard, gbc, row++, "Start Time (yyyy-MM-dd HH:mm):", txtStartTime);
        addFormField(formCard, gbc, row++, "End Time (yyyy-MM-dd HH:mm):", txtEndTime);

        txtPurpose.setLineWrap(true);
        txtPurpose.setWrapStyleWord(true);
        JScrollPane scrollPurpose = new JScrollPane(txtPurpose);
        addFormField(formCard, gbc, row++, "Academic / Project Purpose:", scrollPurpose);
        addFormField(formCard, gbc, row++, "Remarks / Additional Notes:", txtRemarks);

        add(formCard, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.Style.SECONDARY);
        btnCancel.addActionListener(e -> {
            if (parentWindow != null) parentWindow.dispose();
        });

        ModernButton btnBook = new ModernButton("Confirm Booking", ModernButton.Style.PRIMARY);
        btnBook.addActionListener(e -> handleBooking());

        btnPanel.add(btnCancel);
        btnPanel.add(btnBook);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadEquipment() {
        try {
            List<Equipment> list = equipmentDAO.getAllEquipment();
            for (Equipment e : list) {
                cmbEquipment.addItem(e);
                if (preselectedEquipment != null && e.getEquipmentId() == preselectedEquipment.getEquipmentId()) {
                    cmbEquipment.setSelectedItem(e);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load equipment list: " + e.getMessage());
        }
    }

    private void loadUsers() {
        try {
            if (currentUser != null && !currentUser.isAdmin()) {
                cmbUser.addItem(currentUser);
                cmbUser.setEnabled(false);
            } else {
                List<User> users = userDAO.getAllUsers();
                for (User u : users) {
                    cmbUser.addItem(u);
                    if (currentUser != null && u.getUserId() == currentUser.getUserId()) {
                        cmbUser.setSelectedItem(u);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load users list: " + e.getMessage());
        }
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

    private void handleBooking() {
        Equipment eq = (Equipment) cmbEquipment.getSelectedItem();
        User user = (User) cmbUser.getSelectedItem();
        String startStr = txtStartTime.getText().trim();
        String endStr = txtEndTime.getText().trim();
        String purpose = txtPurpose.getText().trim();
        String remarks = txtRemarks.getText().trim();

        if (eq == null || user == null || ValidationUtil.isEmpty(purpose)) {
            JOptionPane.showMessageDialog(this, "Please select equipment, user, and provide a purpose.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("UNDER_MAINTENANCE".equalsIgnoreCase(eq.getStatus())) {
            JOptionPane.showMessageDialog(this, "Selected equipment is currently UNDER MAINTENANCE and cannot be booked.", "Equipment Unavailable", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Timestamp start, end;
        try {
            start = DateUtil.parseDateTime(startStr);
            end = DateUtil.parseDateTime(endStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date/time format. Use 'yyyy-MM-dd HH:mm'.", "Time Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidBookingRange(start, end)) {
            JOptionPane.showMessageDialog(this, "End time must be strictly after start time.", "Invalid Time Window", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (bookingDAO.checkConflict(eq.getEquipmentId(), start, end, null)) {
                JOptionPane.showMessageDialog(this,
                        "Scheduling Conflict! The equipment '" + eq.getName() + "' is already reserved during this time slot.\n" +
                        "Please select a different time window.",
                        "Reservation Conflict", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String initialStatus = currentUser != null && currentUser.isAdmin() ? "APPROVED" : "ACTIVE";
            Booking booking = new Booking(0, eq.getEquipmentId(), user.getUserId(), start, end, purpose, initialStatus, remarks);

            if (bookingDAO.create(booking)) {
                equipmentDAO.updateStatus(eq.getEquipmentId(), "BOOKED");
                auditDAO.log(user.getUserId(), "BOOKING_CREATED", "BOOKING", booking.getBookingId(),
                        "Booked " + eq.getAssetTag() + " from " + startStr + " to " + endStr);

                JOptionPane.showMessageDialog(this, "Booking confirmed successfully!", "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
                saved = true;
                if (parentWindow != null) parentWindow.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
