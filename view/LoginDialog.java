package com.hms.view;

import com.hms.controller.HMSController;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginDialog extends JFrame {

    // --- Modern Style Constants ---
    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color TEXT_DARK = new Color(50, 50, 60);
    private static final Color TEXT_HINT = new Color(150, 150, 160);
    private static final Color PRIMARY_GRADIENT_1 = new Color(66, 133, 244);
    private static final Color PRIMARY_GRADIENT_2 = new Color(138, 180, 248);

    private HMSController controller;
    private JTextField idField;
    private JButton loginBtn;

    public LoginDialog(HMSController controller) {
        this.controller = controller;
        
        // Window Setup
        setTitle("HMS Login");
        setSize(400, 320); // Slightly taller for better spacing
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);
        setLayout(new BorderLayout());

        initComponents();
        
        // Allow "Enter" key to trigger login
        getRootPane().setDefaultButton(loginBtn);
        
        setVisible(true);
    }

    private void initComponents() {
        // --- 1. Top Gradient Header ---
        JPanel headerPanel = new GradientPanel();
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new GridBagLayout()); // To center text vertically

        JLabel titleLabel = new JLabel("Hospital Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Main Input Area ---
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(BG_MAIN);
        contentPanel.setLayout(null); // Absolute positioning for simple login form

        // Label
        JLabel userLabel = new JLabel("User ID");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(TEXT_DARK);
        userLabel.setBounds(50, 30, 100, 20);
        contentPanel.add(userLabel);

        // Input Field (Styled)
        idField = new JTextField();
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        idField.setBounds(50, 55, 280, 40);
        idField.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200)), 
                new EmptyBorder(5, 10, 5, 10))); // Padding inside text field
        contentPanel.add(idField);

        // Hint Text
        JLabel hintLabel = new JLabel("Demo IDs: P001, C002, ST001, A001");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintLabel.setForeground(TEXT_HINT);
        hintLabel.setBounds(50, 100, 300, 20);
        contentPanel.add(hintLabel);

        // Login Button
        loginBtn = new GradientButton("Login to System");
        loginBtn.setBounds(50, 140, 280, 45);
        loginBtn.addActionListener(e -> performLogin());
        contentPanel.add(loginBtn);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void performLogin() {
        String userId = idField.getText().trim();
        if(userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your User ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Attempt login via controller
        String role = controller.login(userId);

        if (role != null) {
            // Success
            this.dispose(); // Close login window
            
            // Open Main View
            // Note: Assuming MainView constructor matches this signature
            SwingUtilities.invokeLater(() -> 
                new MainView(controller, role).setVisible(true)
            );
        } else {
            JOptionPane.showMessageDialog(this, "User ID not found.\nPlease try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Helper Components ---

    // 1. Gradient Panel (Header)
    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, PRIMARY_GRADIENT_1, w, 0, PRIMARY_GRADIENT_2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }

    // 2. Gradient Button (Primary Action)
    private class GradientButton extends JButton {
        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Hover effect logic
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    // Simple logic: we'll handle visual change in paintComponent if needed, 
                    // or just rely on cursor. For a simpler effect, we can shift text color or opacity.
                    setFont(new Font("Segoe UI", Font.BOLD, 16)); // Slight pop
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setFont(new Font("Segoe UI", Font.BOLD, 15));
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Paint Gradient Background
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, PRIMARY_GRADIENT_1, w, 0, PRIMARY_GRADIENT_2);
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, w, h, 10, 10); // Rounded corners

            super.paintComponent(g);
        }
    }
}