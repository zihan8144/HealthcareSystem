package com.hms.view;

import com.hms.controller.HMSController;
import com.hms.model.Referral;
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

public class ReferralPanel extends JPanel {

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

    public ReferralPanel(HMSController controller) {
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

        JLabel titleLabel = new JLabel("Referral Management");
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
            btnPanel.add(refreshBtn);
        } else {
            // Staff View
            JButton addBtn = createHeaderButton("Create");
            JButton editBtn = createHeaderButton("Edit");
            JButton delBtn = createHeaderButton("Delete");
            
            addBtn.addActionListener(e -> showReferralDialog(null));

            editBtn.addActionListener(e -> {
                int r = table.getSelectedRow();
                if(r >= 0) {
                    Referral ref = getReferralFromRow(r);
                    showReferralDialog(ref);
                } else {
                    showWarning("Please select a referral to edit.");
                }
            });
            
            delBtn.addActionListener(e -> {
                 int r = table.getSelectedRow();
                 if(r >= 0) {
                     String id = (String)tableModel.getValueAt(r,0);
                     int confirm = JOptionPane.showConfirmDialog(this, "Delete Referral " + id + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                     if(confirm == JOptionPane.YES_OPTION) {
                         controller.deleteReferral(id);
                         refreshTable();
                     }
                 } else {
                     showWarning("Please select a referral to delete.");
                 }
            });
            
            btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(delBtn);
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

        String[] cols = {"Ref ID", "Patient", "From", "To", "Urgency", "Summary", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
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
        List<Referral> list = controller.getReferrals();
        if (list != null) {
            for(Referral r : list) {
                tableModel.addRow(new Object[]{
                    r.getReferralId(), r.getPatientId(), r.getReferringClinicianId(),
                    r.getReferredToClinicianId(), r.getUrgency(), r.getSummary(), r.getStatus()
                });
            }
        }
    }

    private void showReferralDialog(Referral existing) {
        JTextField idF = new JTextField(existing==null ? "" : existing.getReferralId());
        if(existing!=null) idF.setEditable(false);
        
        List<Patient> patients = controller.getPatients();
        String[] patOpts = patients.stream().map(p -> p.getFullName() + " - " + p.getPatientId()).toArray(String[]::new);
        
        List<Staff> staff = controller.getStaffMembers();
        String[] docOpts = staff.stream()
             .filter(s -> s.getRole().contains("GP") || s.getRole().contains("Specialist"))
             .map(s -> s.getName() + " - " + s.getStaffId()).toArray(String[]::new);
        
        JComboBox<String> pBox = new JComboBox<>(patOpts);
        JComboBox<String> fBox = new JComboBox<>(docOpts); 
        JComboBox<String> tBox = new JComboBox<>(docOpts); 
        pBox.setBackground(Color.WHITE);
        fBox.setBackground(Color.WHITE);
        tBox.setBackground(Color.WHITE);
        
        if(existing!=null) {
            setSelectedOption(pBox, existing.getPatientId());
            setSelectedOption(fBox, existing.getReferringClinicianId());
            setSelectedOption(tBox, existing.getReferredToClinicianId());
        }

        String[] urgs = {"Routine", "Urgent", "Critical"};
        JComboBox<String> uBox = new JComboBox<>(urgs);
        uBox.setBackground(Color.WHITE);
        if(existing!=null) uBox.setSelectedItem(existing.getUrgency());
        
        JTextArea summaryArea = new JTextArea(existing==null ? "" : existing.getSummary(), 5, 30);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane summaryScroll = new JScrollPane(summaryArea);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Referral ID:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; panel.add(idF, gbc); gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Patient:"), gbc);
        gbc.gridx = 1; panel.add(pBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("From Clinician:"), gbc);
        gbc.gridx = 1; panel.add(fBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("To Clinician:"), gbc);
        gbc.gridx = 1; panel.add(tBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Urgency:"), gbc);
        gbc.gridx = 1; panel.add(uBox, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridheight = 2; 
        panel.add(new JLabel("Summary:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridheight = 2; gbc.fill = GridBagConstraints.BOTH; 
        panel.add(summaryScroll, gbc);

        String title = (existing == null) ? "Create Referral" : "Edit Referral";
        
        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if(result == JOptionPane.OK_OPTION) {
            if (idF.getText().trim().isEmpty() || extractId(pBox).isEmpty()) {
                showWarning("Referral ID and Patient are required.");
                return;
            }

            Referral newR = new Referral(
                idF.getText(), extractId(pBox), extractId(fBox), extractId(tBox),
                (String)uBox.getSelectedItem(), summaryArea.getText().trim()
            );
            
            if(existing==null) controller.addReferral(newR);
            else controller.updateReferral(newR);
            
            refreshTable();
        }
    }
    
    // --- Helper Methods ---
    
    private Referral getReferralFromRow(int r) {
        return new Referral(
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