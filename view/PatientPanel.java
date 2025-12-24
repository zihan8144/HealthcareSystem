package com.hms.view;

import com.hms.controller.HMSController;
import com.hms.model.Patient;
import com.hms.model.Staff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PatientPanel extends JPanel {

    // --- Style Constants ---
    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color TABLE_HEAD_BG = new Color(255, 255, 255);
    private static final Color TABLE_ALT_ROW = new Color(248, 250, 252);
    private static final Color TEXT_DARK = new Color(50, 50, 60);
    private static final Color PRIMARY_GRADIENT_1 = new Color(66, 133, 244);
    private static final Color PRIMARY_GRADIENT_2 = new Color(138, 180, 248);

    private HMSController controller;
    private DefaultTableModel tableModel;
    private JTable table;

    public PatientPanel(HMSController controller) {
        this.controller = controller;
        initComponents();
        refreshTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_MAIN);

        // --- 1. Top Gradient Header ---
        JPanel topPanel = new GradientPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        topPanel.setPreferredSize(new Dimension(0, 80));

        JLabel titleLabel = new JLabel("Patient Registry");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Toolbar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = createHeaderButton("Add Patient");
        JButton editBtn = createHeaderButton("Edit");
        JButton delBtn = createHeaderButton("Delete");
        JButton refBtn = createHeaderButton("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(Box.createHorizontalStrut(15));
        btnPanel.add(refBtn);

        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Main Content (Table) ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_MAIN);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Setup Columns
        String[] columnNames = {"ID", "First Name", "Last Name", "DOB", "NHS No", "Phone", "GP ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.setBackground(Color.WHITE);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // --- Event Listeners ---
        
        // 1. Add
        addBtn.addActionListener(e -> showPatientDialog(null));

        // 2. Edit
        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                // reconstruct object from selected row for editing
                String id = (String) tableModel.getValueAt(selectedRow, 0);
                String fn = (String) tableModel.getValueAt(selectedRow, 1);
                String ln = (String) tableModel.getValueAt(selectedRow, 2);
                String dob = (String) tableModel.getValueAt(selectedRow, 3);
                String nhs = (String) tableModel.getValueAt(selectedRow, 4);
                String phone = (String) tableModel.getValueAt(selectedRow, 5);
                String gpId = (String) tableModel.getValueAt(selectedRow, 6);
                
                Patient existing = new Patient(id, fn, ln, dob, nhs, "Unknown", phone, gpId);
                showPatientDialog(existing);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a patient to edit.");
            }
        });

        // 3. Delete
        delBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String id = (String) tableModel.getValueAt(selectedRow, 0);
                String name = (String) tableModel.getValueAt(selectedRow, 1);
                
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "Delete Patient " + name + " (" + id + ")?", 
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.deletePatient(id);
                    refreshTable(); 
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a patient to delete.");
            }
        });

        // 4. Refresh
        refBtn.addActionListener(e -> refreshTable());
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Patient> patients = controller.getPatients();
        if (patients != null) {
            for (Patient p : patients) {
                tableModel.addRow(new Object[]{
                    p.getPatientId(), p.getFirstName(), p.getLastName(),
                    p.getDob(), p.getNhsNumber(), p.getPhoneNumber(), p.getGpSurgeryId()
                });
            }
        }
    }

    private void showPatientDialog(Patient existing) {
        JTextField idF = new JTextField(existing == null ? "" : existing.getPatientId());
        JTextField fnF = new JTextField(existing == null ? "" : existing.getFirstName());
        JTextField lnF = new JTextField(existing == null ? "" : existing.getLastName());
        JTextField dobF = new JTextField(existing == null ? "" : existing.getDob());
        JTextField nhsF = new JTextField(existing == null ? "" : existing.getNhsNumber());
        JTextField phF = new JTextField(existing == null ? "" : existing.getPhoneNumber());

        List<Staff> staffList = controller.getStaffMembers();
        String[] gpOptions = new String[0];
        
        if (staffList != null) {
            gpOptions = staffList.stream()
                .filter(s -> s.getRole().contains("GP") || s.getRole().contains("Doctor") || s.getRole().contains("General")) 
                .map(s -> s.getName() + " - " + s.getStaffId()) 
                .toArray(String[]::new);
        }

        JComboBox<String> gpBox = new JComboBox<>(gpOptions);
        gpBox.setEditable(true); 
        gpBox.setBackground(Color.WHITE);

        if (existing != null) {
            idF.setEditable(false);
            setSelectedOption(gpBox, existing.getGpSurgeryId());
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("ID:")); panel.add(idF);
        panel.add(new JLabel("First Name:")); panel.add(fnF);
        panel.add(new JLabel("Last Name:")); panel.add(lnF);
        panel.add(new JLabel("DOB (YYYY-MM-DD):")); panel.add(dobF);
        panel.add(new JLabel("NHS No:")); panel.add(nhsF);
        panel.add(new JLabel("Phone:")); panel.add(phF);
        panel.add(new JLabel("Registered GP:")); panel.add(gpBox);

        String title = (existing == null) ? "Add Patient" : "Edit Patient";
        
        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String selectedGpId = extractId(gpBox); 

            if (idF.getText().trim().isEmpty() || fnF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID and First Name are required.");
                return;
            }

            Patient newP = new Patient(
                idF.getText(), fnF.getText(), lnF.getText(), dobF.getText(),
                nhsF.getText(), "Unknown", phF.getText(), selectedGpId
            );

            if (existing == null) controller.addPatient(newP);
            else controller.updatePatient(newP);
            
            refreshTable();
        }
    }

    // --- Helper Methods ---
    
    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEAD_BG);
        header.setForeground(new Color(100, 100, 100));
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 10, 0, 0));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALT_ROW);
                }
                return c;
            }
        });
    }


    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(255, 255, 255, 0));
        btn.setBorder(new LineBorder(Color.WHITE, 1));
        
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(Color.WHITE);
                btn.setForeground(PRIMARY_GRADIENT_1);
                btn.setOpaque(true);
            }
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
                btn.setBackground(new Color(255, 255, 255, 0));
                btn.setForeground(Color.WHITE);
                btn.setOpaque(false);
            }
        });
        return btn;
    }

    private String extractId(JComboBox<String> box) {
        String raw = (String) box.getSelectedItem();
        if (raw != null && raw.contains(" - ")) {
            String[] parts = raw.split(" - ");
            return parts[parts.length - 1].trim(); 
        }
        return raw != null ? raw.trim() : "";
    }

    private void setSelectedOption(JComboBox<String> box, String targetId) {
        if(targetId == null) return;
        for (int i = 0; i < box.getItemCount(); i++) {
            String item = box.getItemAt(i);
            if (item != null && (item.endsWith(" - " + targetId) || item.equals(targetId))) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

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
}