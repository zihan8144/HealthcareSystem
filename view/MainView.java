package com.hms.view;

import com.hms.controller.HMSController;
import com.hms.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class MainView extends JFrame {
    
    // --- Style Constants ---
    private static final Color SIDEBAR_BG = Color.WHITE;
    private static final Color ACTIVE_BG = new Color(232, 240, 254); // Light Blue
    private static final Color ACTIVE_TEXT = new Color(25, 103, 210); // Dark Blue
    private static final Color INACTIVE_TEXT = new Color(95, 99, 104); // Grey
    private static final Font MENU_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private HMSController controller;
    private String currentUserRole;
    
    // Layout Components
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private Map<String, JButton> menuButtons = new HashMap<>();
    private String currentCardName = "";

    public MainView(HMSController controller, String role) {
        this.controller = controller;
        this.currentUserRole = role;

        // Window Setup
        setTitle("Healthcare Management System - " + role);
        setSize(1200, 800); // Widescreen format
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main Layout: Sidebar (West) + Content (Center)
        setLayout(new BorderLayout());

        // 1. Initialize Panels
        initSidebar();
        initContentArea();

        // 2. Load Modules based on Role
        loadRoleBasedModules();

        // 3. Select first item by default
        if (!menuButtons.isEmpty()) {
            String firstKey = menuButtons.keySet().iterator().next();
            switchModule(firstKey);
        }

        setVisible(true);
    }

    private void initSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setBorder(new MatteBorder(0, 0, 0, 1, new Color(230, 230, 230))); // Right border

        // -- App Logo / Title Area --
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setMaximumSize(new Dimension(240, 80));
        
        JLabel appTitle = new JLabel("HMS Portal");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(ACTIVE_TEXT);
        logoPanel.add(appTitle);
        sidebarPanel.add(logoPanel);
        
        // -- Menu Items Container --
        // We will add buttons here dynamically later
        
        add(sidebarPanel, BorderLayout.WEST);
    }

    private void initContentArea() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(245, 247, 250)); // Match panel backgrounds
        add(contentPanel, BorderLayout.CENTER);
    }

    private void loadRoleBasedModules() {
        // --- Dynamic Menu Generation ---
        
        // 1. Patient View
        if ("Patient".equals(currentUserRole)) {
            addModule("Appointments", new AppointmentPanel(controller));
            addModule("Prescriptions", new PrescriptionPanel(controller));
            addModule("Referrals", new ReferralPanel(controller));
        } 
        // 2. Doctors (GP / Specialist)
        else if ("GP".equals(currentUserRole) || "Specialist".equals(currentUserRole)) {
            addModule("My Patients", new PatientPanel(controller));
            addModule("Appointments", new AppointmentPanel(controller));
            addModule("Prescriptions", new PrescriptionPanel(controller));
            addModule("Referrals", new ReferralPanel(controller));
        } 
        // 3. Admin
        else if ("Admin".equals(currentUserRole)) {
            addModule("Staff Manager", new AdminPanel(controller)); 
            addModule("Patient Records", new PatientPanel(controller));
            addModule("Appointments", new AppointmentPanel(controller));
            addModule("Prescriptions", new PrescriptionPanel(controller));
            addModule("Referrals", new ReferralPanel(controller));
        }
        // 4. Receptionist / Nurse
        else {
            addModule("Patients", new PatientPanel(controller));
            addModule("Appointments", new AppointmentPanel(controller));
            if ("Nurse".equals(currentUserRole)) {
                addModule("Prescriptions", new PrescriptionPanel(controller));
            }
        }

        // --- Bottom User Section (Profile & Logout) ---
        sidebarPanel.add(Box.createVerticalGlue()); // Push to bottom
        addSidebarDivider();
        addBottomButton("Edit Profile", e -> showProfileDialog());
        addBottomButton("Logout", e -> performLogout());
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Bottom padding
    }

    // --- Helper: Add a Navigation Module ---
    private void addModule(String name, JPanel panel) {
        // Add to Card Layout
        contentPanel.add(panel, name);

        // Create Sidebar Button
        JButton btn = createMenuButton(name);
        btn.addActionListener(e -> switchModule(name));
        
        menuButtons.put(name, btn);
        sidebarPanel.add(btn);
    }

    private void switchModule(String name) {
        // 1. Update UI (Card)
        cardLayout.show(contentPanel, name);
        currentCardName = name;

        // 2. Update Sidebar Visuals (Active State)
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(name)) {
                btn.setBackground(ACTIVE_BG);
                btn.setForeground(ACTIVE_TEXT);
                btn.setFont(MENU_FONT); // Bold
            } else {
                btn.setBackground(SIDEBAR_BG);
                btn.setForeground(INACTIVE_TEXT);
                btn.setFont(MENU_FONT.deriveFont(Font.PLAIN));
            }
        }
    }

    // --- Component Factories ---
    
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(MENU_FONT.deriveFont(Font.PLAIN));
        btn.setForeground(INACTIVE_TEXT);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(new EmptyBorder(12, 25, 12, 0)); // Text padding
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(240, 50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Simple Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!btn.getText().equals(currentCardName)) {
                   btn.setBackground(new Color(245, 245, 245));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!btn.getText().equals(currentCardName)) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });
        return btn;
    }

    private void addBottomButton(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(100, 100, 100)); // Darker grey
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(new EmptyBorder(10, 25, 10, 0));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(240, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (text.equals("Logout")) {
            btn.setForeground(new Color(217, 48, 37)); // Red for logout
        }
        
        btn.addActionListener(action);
        sidebarPanel.add(btn);
    }
    
    private void addSidebarDivider() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 1));
        sep.setForeground(new Color(230, 230, 230));
        sidebarPanel.add(sep);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    // --- Business Logic (Preserved) ---

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginDialog(controller);
        }
    }

    private void showProfileDialog() {
        if ("Patient".equals(currentUserRole)) {
            Patient p = controller.getCurrentPatient();
            if (p == null) return;

            // Use simplified layout for dialog
            JPanel pPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            JTextField fnF = new JTextField(p.getFirstName());
            JTextField lnF = new JTextField(p.getLastName());
            JTextField phF = new JTextField(p.getPhoneNumber());
            JTextField idF = new JTextField(p.getPatientId()); idF.setEditable(false);
            
            pPanel.add(new JLabel("ID:")); pPanel.add(idF);
            pPanel.add(new JLabel("First Name:")); pPanel.add(fnF);
            pPanel.add(new JLabel("Last Name:")); pPanel.add(lnF);
            pPanel.add(new JLabel("Phone:")); pPanel.add(phF);

            if (JOptionPane.showConfirmDialog(this, pPanel, "Edit Profile", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                Patient newP = new Patient(
                    p.getPatientId(), fnF.getText(), lnF.getText(), 
                    p.getDob(), p.getNhsNumber(), "Unknown", phF.getText(), p.getRegisteredGpId()
                );
                controller.updatePatient(newP);
                JOptionPane.showMessageDialog(this, "Profile Updated!");
            }

        } else {
            // Staff Profile
            Staff s = controller.getCurrentStaff();
            if (s == null) return;

            JPanel sPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            JTextField fnF = new JTextField();
            JTextField lnF = new JTextField();
            JTextField idF = new JTextField(s.getStaffId()); idF.setEditable(false);
            JTextField roleF = new JTextField(s.getRole()); roleF.setEditable(false);

            String[] parts = s.getName().split(" ", 2);
            fnF.setText(parts[0]);
            if (parts.length > 1) lnF.setText(parts[1]);

            sPanel.add(new JLabel("ID:")); sPanel.add(idF);
            sPanel.add(new JLabel("Role:")); sPanel.add(roleF);
            sPanel.add(new JLabel("First Name:")); sPanel.add(fnF);
            sPanel.add(new JLabel("Last Name:")); sPanel.add(lnF);

            if (JOptionPane.showConfirmDialog(this, sPanel, "Edit Staff Profile", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                Staff newS;
                String id = s.getStaffId();
                String f = fnF.getText();
                String l = lnF.getText();
                String dept = s.getDepartment();

                // Polymorphic update logic (Preserved)
                if (s instanceof Admin) newS = new Admin(id, f, l, dept);
                else if (s instanceof Receptionist) newS = new Receptionist(id, f, l, dept);
                else if (s instanceof Nurse) newS = new Nurse(id, f, l, dept);
                else if (s instanceof GeneralPractitioner) {
                    GeneralPractitioner gp = (GeneralPractitioner) s;
                    newS = new GeneralPractitioner(id, f, l, dept, gp.getSpecialty(), "GMC-XXX");
                } else if (s instanceof Specialist) {
                    Specialist sp = (Specialist) s;
                    newS = new Specialist(id, f, l, dept, sp.getSpecialty());
                } else {
                    newS = new Staff(id, f, l, s.getRole(), dept);
                }

                controller.updateStaff(newS);
                JOptionPane.showMessageDialog(this, "Profile Updated!");
            }
        }
    }
}