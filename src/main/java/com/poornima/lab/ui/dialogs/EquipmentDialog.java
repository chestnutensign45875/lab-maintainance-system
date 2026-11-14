package com.poornima.lab.ui.dialogs;

import com.poornima.lab.model.Equipment;
import com.poornima.lab.model.User;

import javax.swing.*;
import java.awt.*;

public class EquipmentDialog extends JDialog {
    private final EquipmentFormPanel panel;

    public EquipmentDialog(Window owner, Equipment equipment, User currentUser) {
        super(owner, equipment == null ? "Add New Lab Asset" : "Edit Equipment Details", ModalityType.APPLICATION_MODAL);
        setSize(520, 620);
        setLocationRelativeTo(owner);
        panel = new EquipmentFormPanel(this, equipment, currentUser);
        setContentPane(panel);
    }

    public boolean isSaved() {
        return panel.isSaved();
    }
}
