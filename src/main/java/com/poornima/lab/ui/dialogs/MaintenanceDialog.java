package com.poornima.lab.ui.dialogs;

import com.poornima.lab.model.MaintenanceRecord;
import com.poornima.lab.model.User;

import javax.swing.*;
import java.awt.*;

public class MaintenanceDialog extends JDialog {
    private final MaintenanceFormPanel panel;

    public MaintenanceDialog(Window owner, MaintenanceRecord record, User currentUser) {
        super(owner, record == null ? "Log New Equipment Maintenance" : "Update Service Record", ModalityType.APPLICATION_MODAL);
        setSize(520, 580);
        setLocationRelativeTo(owner);
        panel = new MaintenanceFormPanel(this, record, currentUser);
        setContentPane(panel);
    }

    public boolean isSaved() {
        return panel.isSaved();
    }
}
