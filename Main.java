package com.hms;

import com.hms.controller.HMSController;
import com.hms.view.LoginDialog;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. Enable Font Anti-aliasing (Makes text look smoother)
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            // 2. Set System Look and Feel
            // This ensures window borders, scrollbars, and file choosers look native to the OS (Windows/Mac)
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // If failed, fallback to default (Metal), log error but continue
                System.err.println("Failed to initialize System Look and Feel");
                e.printStackTrace();
            }

            // 3. Initialize Controller
            HMSController controller = new HMSController();
            
            // 4. Launch Login Dialog
            // Note: LoginDialog constructor is expected to setVisible(true)
            new LoginDialog(controller);
        });
    }
}