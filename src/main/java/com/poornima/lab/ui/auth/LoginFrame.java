package com.poornima.lab.ui.auth;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        super("Poornima Institute - Lab Equipment Booking & Maintenance System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 560);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);

        setContentPane(new LoginPanel(this));
    }
}
