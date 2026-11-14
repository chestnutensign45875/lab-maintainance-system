package com.poornima.lab.ui.dialogs;

import javax.swing.*;
import java.awt.*;

public class ExportDialog extends JDialog {
    public ExportDialog(Window owner) {
        super(owner, "System Reports & Data Export", ModalityType.APPLICATION_MODAL);
        setSize(780, 580);
        setLocationRelativeTo(owner);
        setContentPane(new ExportPanel(this));
    }
}
