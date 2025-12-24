package com.hms.view;

import com.hms.controller.HMSController;
import com.hms.model.Prescription;
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

public class PrescriptionPanel extends JPanel {

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

    public PrescriptionPanel(HMSController controller) {
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

        JLabel titleLabel = new JLabel("Prescription Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Toolbar Container
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        // --- Dynamic Buttons based on Role ---
        String role = controller.getCurrentUserRole();
        JButton refreshBtn = createHeaderButton("Refresh");

        if ("Patient".equals(role)) {
            // Patient View
            JButton refillBtn = createHeaderButton("Request Refill");
            refillBtn.setPreferredSize(new Dimension(130, 32)); // Wider button
            refillBtn.addActionListener(e -> requestRefill());
            btnPanel.add(refillBtn);
        } else {
            // Staff View
            JButton addBtn = createHeaderButton("Issue New");
            JButton editBtn = createHeaderButton("Edit");
            JButton printBtn = createHeaderButton("Print to File");
            JButton delBtn = createHeaderButton("Delete");

            addBtn.addActionListener(e -> showDialog(null));

            editBtn.addActionListener(e -> {
                int r = table.getSelectedRow();
                if(r >= 0) {
                    Prescription p = getPrescriptionFromRow(r);
                    showDialog(p);
                } else {
                    showWarning("Please select a prescription to edit.");
                }
            });

            printBtn.addActionListener(e -> {
                int r = table.getSelectedRow();
                if(r >= 0) {
                    Prescription p = getPrescriptionFromRow(r);
                    controller.printPrescription(p);
                    JOptionPane.showMessageDialog(this, "Prescription details saved to text file successfully.");
                } else {
                    showWarning("Please select a prescription to print.");
                }
            });

            delBtn.addActionListener(e -> {
                int r = table.getSelectedRow();
                if(r >= 0) {
                    String id = (String)tableModel.getValueAt(r,0);
                    int confirm = JOptionPane.showConfirmDialog(this, "Delete Prescription " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if(confirm == JOptionPane.YES_OPTION) {
                        controller.deletePrescription(id);
                        refreshTable();
                    }
                } else {
                    showWarning("Please select a prescription to delete.");
                }
            });

            btnPanel.add(addBtn);
            btnPanel.add(editBtn);
            btnPanel.add(printBtn);
            btnPanel.add(delBtn);
        }

        btnPanel.add(Box.createHorizontalStrut(15));
        btnPanel.add(refreshBtn);
        
        refreshBtn.addActionListener(e -> refreshTable());

        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Main Content (Table) ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_MAIN);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        String[] cols = {"ID", "Patient", "Clinician", "Medication", "Dosage", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
             @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.setBackground(Color.WHITE);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    // --- Logic Methods ---

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Prescription> list = controller.getPrescriptions();
        if (list != null) {
            for(Prescription p : list) {
                tableModel.addRow(new Object[]{
                    p.getPrescriptionId(), p.getPatientId(), p.getClinicianId(),
                    p.getMedication(), p.getDosage(), p.getStatus()
                });
            }
        }
    }
    
    private void requestRefill() {
        int r = table.getSelectedRow();
        if(r >= 0) {
             Prescription p = getPrescriptionFromRow(r);
             Prescription refillRequest = new Prescription(
                p.getPrescriptionId(), p.getPatientId(), p.getClinicianId(), 
                p.getMedication(), p.getDosage(), "Refill Requested"
             );
             
             controller.requestRefill(refillRequest);
             JOptionPane.showMessageDialog(this, "Refill request sent to clinician!");
             refreshTable();
        } else {
            showWarning("Select a prescription to refill.");
        }
    }

    private void showDialog(Prescription existing) {
        JTextField idF = new JTextField(existing==null ? "" : existing.getPrescriptionId());
        if(existing!=null) idF.setEditable(false);
        
        List<Patient> patients = controller.getPatients();
        String[] patOpts = patients.stream().map(p -> p.getFullName() + " - " + p.getPatientId()).toArray(String[]::new);
        
        List<Staff> staff = controller.getStaffMembers();
        String[] docOpts = staff.stream()
             .filter(s -> s.getRole().contains("GP") || s.getRole().contains("Specialist"))
             .map(s -> s.getName() + " - " + s.getStaffId()).toArray(String[]::new);
             
        JComboBox<String> pBox = new JComboBox<>(patOpts);
        JComboBox<String> cBox = new JComboBox<>(docOpts);
        pBox.setBackground(Color.WHITE);
        cBox.setBackground(Color.WHITE);

        if(existing!=null) { 
            setSelectedOption(pBox, existing.getPatientId()); 
            setSelectedOption(cBox, existing.getClinicianId()); 
        }

        JTextField mF = new JTextField(existing==null ? "" : existing.getMedication());
        JTextField dF = new JTextField(existing==null ? "" : existing.getDosage());
        
        String[] stats = {"Issued", "Refill Requested", "Dispensed", "Revoked"};
        JComboBox<String> sBox = new JComboBox<>(stats);
        sBox.setEditable(true); 
        sBox.setBackground(Color.WHITE);
        if(existing!=null) sBox.setSelectedItem(existing.getStatus());

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Prescription ID:")); panel.add(idF);
        panel.add(new JLabel("Patient:")); panel.add(pBox);
        panel.add(new JLabel("Clinician:")); panel.add(cBox);
        panel.add(new JLabel("Medication:")); panel.add(mF);
        panel.add(new JLabel("Dosage:")); panel.add(dF);
        panel.add(new JLabel("Status:")); panel.add(sBox);

        String title = (existing == null) ? "Issue Prescription" : "Edit Prescription";
        
        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if(result == JOptionPane.OK_OPTION) {
            if(idF.getText().trim().isEmpty() || mF.getText().trim().isEmpty()) {
                showWarning("ID and Medication are required.");
                return;
            }

            Prescription newP = new Prescription(
                idF.getText(), extractId(pBox), extractId(cBox),
                mF.getText(), dF.getText(), (String)sBox.getSelectedItem()
            );
            
            if(existing==null) controller.addPrescription(newP);
            else controller.updatePrescription(newP);
            
            refreshTable();
        }
    }
    
    // --- Helper Methods ---

    private Prescription getPrescriptionFromRow(int r) {
        return new Prescription(
            (String)tableModel.getValueAt(r,0), (String)tableModel.getValueAt(r,1),
            (String)tableModel.getValueAt(r,2), (String)tableModel.getValueAt(r,3),
            (String)tableModel.getValueAt(r,4), (String)tableModel.getValueAt(r,5)
        );
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Action Required", JOptionPane.WARNING_MESSAGE);
    }

    private String extractId(JComboBox<String> b) {
        String s = (String)b.getSelectedItem(); 
        return (s!=null && s.contains(" - ")) ? s.split(" - ")[1].trim() : s;
    }

    private void setSelectedOption(JComboBox<String> b, String id) {
        for(int i=0; i<b.getItemCount(); i++) {
            if(b.getItemAt(i).endsWith(" - "+id)) {
                b.setSelectedIndex(i);
                return;
            }
        }
    }

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
        btn.setPreferredSize(new Dimension(110, 32)); // Default width
        
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