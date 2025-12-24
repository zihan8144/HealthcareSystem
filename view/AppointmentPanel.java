package com.hms.view;

import com.hms.controller.HMSController;
import com.hms.model.Appointment;
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

public class AppointmentPanel extends JPanel {

    // --- Style Constants (Same as AdminPanel) ---
    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color TABLE_HEAD_BG = new Color(255, 255, 255);
    private static final Color TABLE_ALT_ROW = new Color(248, 250, 252);
    private static final Color TEXT_DARK = new Color(50, 50, 60);
    private static final Color PRIMARY_GRADIENT_1 = new Color(66, 133, 244);
    private static final Color PRIMARY_GRADIENT_2 = new Color(138, 180, 248);

    private HMSController controller;
    private DefaultTableModel tableModel;
    private JTable table;

    public AppointmentPanel(HMSController controller) {
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

        JLabel titleLabel = new JLabel("Appointment Schedule");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Toolbar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = createHeaderButton("Book Appointment");
        JButton editBtn = createHeaderButton("Edit");
        JButton deleteBtn = createHeaderButton("Delete");
        JButton refreshBtn = createHeaderButton("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(Box.createHorizontalStrut(15));
        btnPanel.add(refreshBtn);

        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Main Content (Table) ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_MAIN);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        String[] cols = {"Appt ID", "Patient ID", "Clinician ID", "Date", "Time", "Reason", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
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
        addBtn.addActionListener(e -> showDialog(null));

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                // Reconstruct object from table
                Appointment appt = new Appointment(
                    (String)tableModel.getValueAt(row, 0),
                    (String)tableModel.getValueAt(row, 1),
                    (String)tableModel.getValueAt(row, 2),
                    (String)tableModel.getValueAt(row, 3),
                    (String)tableModel.getValueAt(row, 4),
                    (String)tableModel.getValueAt(row, 5)
                );
                
                showDialog(appt);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an appointment to edit.");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = (String) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to cancel Appointment " + id + "?", 
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.deleteAppointment(id);
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an appointment to delete.");
            }
        });

        refreshBtn.addActionListener(e -> refreshTable());
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Appointment> list = controller.getAppointments();
        if (list != null) {
            for(Appointment a : list) {
                tableModel.addRow(new Object[]{
                    a.getAppointmentId(), a.getPatientId(), a.getClinicianId(), 
                    a.getDate(), a.getTime(), a.getReason(), a.getStatus()
                });
            }
        }
    }

    private void showDialog(Appointment existing) {
        // --- 1. Form Inputs ---
        JTextField idF = new JTextField(existing == null ? "" : existing.getAppointmentId());
        if(existing != null) idF.setEditable(false);

        JComponent patInput, docInput;
        String role = controller.getCurrentUserRole();
        String myId = controller.getCurrentUserId();

        // Logic A: Patient Field
        if ("Patient".equals(role)) {
            // Patient can only book for self
            patInput = new JTextField(myId);
            ((JTextField)patInput).setEditable(false);
        } else {
            // Admin/Staff can select patient
            List<Patient> pats = controller.getPatients(); 
            String[] patOpts = new String[0];
            if(pats != null) {
                patOpts = pats.stream()
                    .map(p -> p.getFullName() + " - " + p.getPatientId())
                    .toArray(String[]::new);
            }
            JComboBox<String> box = new JComboBox<>(patOpts);
            box.setBackground(Color.WHITE);
            if(existing != null) setSelectedOption(box, existing.getPatientId());
            patInput = box;
        }

        // Logic B: Clinician Field
        // Get all staff who are doctors
        List<Staff> staff = controller.getStaffMembers();
        String[] docOpts = new String[0];
        if(staff != null) {
            docOpts = staff.stream()
                .filter(s -> s.getRole().contains("GP") || s.getRole().contains("Specialist"))
                .map(s -> s.getName() + " - " + s.getStaffId())
                .toArray(String[]::new);
        }

        if ("GP".equals(role) || "Specialist".equals(role)) {
            // If doctor is booking, pre-select self
            JComboBox<String> box = new JComboBox<>(docOpts);
            box.setBackground(Color.WHITE);
            setSelectedOption(box, myId); 
            docInput = box;
        } else {
            JComboBox<String> box = new JComboBox<>(docOpts);
            box.setBackground(Color.WHITE);
            if(existing != null) setSelectedOption(box, existing.getClinicianId());
            docInput = box;
        }

        JTextField dateF = new JTextField(existing == null ? "" : existing.getDate());
        JTextField timeF = new JTextField(existing == null ? "" : existing.getTime());
        JTextField reasonF = new JTextField(existing == null ? "" : existing.getReason());

        // --- 2. Layout Panel (Grid) ---
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Appt ID:")); panel.add(idF);
        panel.add(new JLabel("Patient:")); panel.add(patInput);
        panel.add(new JLabel("Clinician:")); panel.add(docInput);
        panel.add(new JLabel("Date (DD/MM/YYYY):")); panel.add(dateF);
        panel.add(new JLabel("Time (HH:MM):")); panel.add(timeF);
        panel.add(new JLabel("Reason:")); panel.add(reasonF);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                existing == null ? "Book Appointment" : "Edit Appointment", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if(result == JOptionPane.OK_OPTION) {
            // Extract IDs
            String pid = (patInput instanceof JComboBox) ? extractId((JComboBox)patInput) : ((JTextField)patInput).getText();
            String cid = (docInput instanceof JComboBox) ? extractId((JComboBox)docInput) : ((JTextField)docInput).getText();
            
            // Validation
            if (idF.getText().trim().isEmpty() || dateF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID and Date are required.");
                return;
            }

            Appointment newA = new Appointment(
                idF.getText(), pid, cid, dateF.getText(), timeF.getText(), reasonF.getText()
            );
            
            if(existing == null) controller.addAppointment(newA);
            else controller.updateAppointment(newA);
            
            refreshTable();
        }
    }

    // --- Helpers (Same as previous style) ---

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
        btn.setPreferredSize(new Dimension(140, 32)); 
        
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

    // Logic Helper: Extract ID from "Name - ID" string
    private String extractId(JComboBox<String> box) {
        String raw = (String) box.getSelectedItem();
        if (raw != null && raw.contains(" - ")) return raw.split(" - ")[1].trim();
        return raw != null ? raw : "";
    }

    // Logic Helper: Select dropdown item by ID suffix
    private void setSelectedOption(JComboBox<String> box, String id) {
        for(int i=0; i<box.getItemCount(); i++) {
            String item = box.getItemAt(i);
            if(item != null && item.endsWith(" - " + id)) { 
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