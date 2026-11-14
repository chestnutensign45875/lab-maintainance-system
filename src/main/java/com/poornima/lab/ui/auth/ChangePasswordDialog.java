package com.poornima.lab.ui.auth;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    public ChangePasswordDialog(Window owner, String prefilledUsername) {
        super(owner, "Change Account Password", ModalityType.APPLICATION_MODAL);
        setSize(420, 380);
        setLocationRelativeTo(owner);
        setContentPane(new ChangePasswordPanel(this, prefilledUsername));
    }
}
