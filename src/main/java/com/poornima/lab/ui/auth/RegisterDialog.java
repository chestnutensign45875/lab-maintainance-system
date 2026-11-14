package com.poornima.lab.ui.auth;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private final RegisterPanel panel;

    public RegisterDialog(Frame owner) {
        super(owner, "Register New User Account", true);
        setSize(480, 560);
        setLocationRelativeTo(owner);
        panel = new RegisterPanel(this);
        setContentPane(panel);
    }

    public boolean isRegistered() {
        return panel.isRegistered();
    }
}
