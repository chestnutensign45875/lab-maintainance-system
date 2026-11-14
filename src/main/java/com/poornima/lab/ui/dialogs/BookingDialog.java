package com.poornima.lab.ui.dialogs;

import com.poornima.lab.model.Equipment;
import com.poornima.lab.model.User;

import javax.swing.*;
import java.awt.*;

public class BookingDialog extends JDialog {
    private final BookingFormPanel panel;

    public BookingDialog(Window owner, User currentUser, Equipment preselectedEquipment) {
        super(owner, "Reserve Laboratory Equipment", ModalityType.APPLICATION_MODAL);
        setSize(520, 560);
        setLocationRelativeTo(owner);
        panel = new BookingFormPanel(this, currentUser, preselectedEquipment);
        setContentPane(panel);
    }

    public boolean isSaved() {
        return panel.isSaved();
    }
}
