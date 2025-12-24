package com.hms.view;

import com.hms.controller.HMSController;
import com.hms.model.*;

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

public class AdminPanel extends JPanel {

    // --- Modern Light Palette ---
    private static final Color BG_MAIN = new Color(245, 247, 250); // Very light grey-blue
    private static final Color TABLE_HEAD_BG = new Color(255, 255, 255);
    private static final Color TABLE_ALT_ROW = new Color(248, 250, 252); // Alternating row color
    private static final Color TEXT_DARK = new Color(50, 50, 60);
    private static final Color PRIMARY_GRADIENT_1 = new Color(66, 133, 244); // Google Blue
    private static final Color PRIMARY_GRADIENT_2 = new Color(138, 180, 248); // Lighter Blue

    private HMSController controller;
    private DefaultTableModel tableModel;
    private JTable table;

    public AdminPanel(HMSController controller) {
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

        // Title
        JLabel titleLabel = new JLabel("Staff Management Portal");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Toolbar Buttons (Transparent background to show gradient)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false); // Make transparent

        // Simple Text Buttons (No Unicode)
        JButton addBtn = createHeaderButton("Add Staff");
        JButton editBtn = createHeaderButton("Edit");
        JButton delBtn = createHeaderButton("Delete");
        JButton logBtn = createHeaderButton("Logs");
        JButton refBtn = createHeaderButton("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(Box.createHorizontalStrut(15));
        btnPanel.add(logBtn);
        btnPanel.add(refBtn);

        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Main Content Area ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_MAIN);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Setup Table
        String[] cols = {"ID", "Full Name", "Role", "Department"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table);

        // ScrollPane with white background
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
                // Fetch data
                String id = (String) tableModel.getValueAt(row, 0);
                String fullName = (String) tableModel.getValueAt(row, 1);
                String role = (String) tableModel.getValueAt(row, 2);
                String dept = (String) tableModel.getValueAt(row, 3);
                
                // Split name back
                String[] parts = fullName.split(" ", 2);
                String fn = parts[0];
                String ln = (parts.length > 1) ? parts[1] : "";

                showDialog(new Staff(id, fn, ln, role, dept));
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            }
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = (String) tableModel.getValueAt(row, 0);
                String name = (String) tableModel.getValueAt(row, 1);
                
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "Are you sure you want to remove:\n" + name + " (" + id + ")?", 
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.deleteStaff(id);
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            }
        });

        logBtn.addActionListener(e -> {
            JTextArea area = new JTextArea(15, 50);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            area.setBorder(new EmptyBorder(10,10,10,10));
            for(String log : controller.getAuditLogs()) area.append(log + "\n");
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "System Logs", JOptionPane.PLAIN_MESSAGE);
        });

        refBtn.addActionListener(e -> refreshTable());
    }

    // --- Styling Helpers ---

    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(232, 240, 254)); // Soft Blue selection
        table.setSelectionForeground(TEXT_DARK);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEAD_BG);
        header.setForeground(new Color(100, 100, 100));
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));

        // Zebra Striping Renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Add Padding
                setBorder(new EmptyBorder(0, 10, 0, 0));
                
                if (!isSelected) {
                    // Alternate row colors
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
        btn.setBackground(new Color(255, 255, 255, 0)); // Alpha = 0
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

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Staff> staffList = controller.getStaffMembers();
        if(staffList != null) {
            for(Staff s : staffList) {
                tableModel.addRow(new Object[]{ 
                    s.getStaffId(), 
                    s.getName(), 
                    s.getRole(), 
                    s.getDepartment() 
                });
            }
        }
    }

    private void showDialog(Staff existing) {
        JTextField idF = new JTextField(existing == null ? "" : existing.getStaffId());
        if(existing != null) idF.setEditable(false);
        
        String oldF = "", oldL = "";
        if(existing != null) {
            String[] parts = existing.getName().split(" ", 2);
            oldF = parts[0]; oldL = (parts.length > 1) ? parts[1] : "";
        }
        
        JTextField fnF = new JTextField(oldF);
        JTextField lnF = new JTextField(oldL);
        JTextField deptF = new JTextField(existing == null ? "" : existing.getDepartment());
        
        String[] roles = {"GP", "Specialist", "Nurse", "Receptionist", "Admin"};
        JComboBox<String> rBox = new JComboBox<>(roles);
        rBox.setBackground(Color.WHITE);
        if(existing != null) {
             for(String r : roles) if(existing.getRole().contains(r)) rBox.setSelectedItem(r);
        }

        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.add(new JLabel("Staff ID:")); p.add(idF);
        p.add(new JLabel("First Name:")); p.add(fnF);
        p.add(new JLabel("Last Name:")); p.add(lnF);
        p.add(new JLabel("Role:")); p.add(rBox);
        p.add(new JLabel("Department:")); p.add(deptF);

        int res = JOptionPane.showConfirmDialog(this, p, existing == null ? "Add Staff" : "Edit Staff", JOptionPane.OK_CANCEL_OPTION);
        
        if(res == JOptionPane.OK_OPTION) {
            String id = idF.getText().trim();
            String f = fnF.getText().trim();
            String l = lnF.getText().trim();
            String d = deptF.getText().trim();
            String role = (String)rBox.getSelectedItem();

            if(id.isEmpty() || f.isEmpty()) return;

            Staff newS;
            if ("Admin".equals(role)) newS = new Admin(id, f, l, d);
            else if ("Receptionist".equals(role)) newS = new Receptionist(id, f, l, d);
            else if ("Nurse".equals(role)) newS = new Nurse(id, f, l, d);
            else if ("GP".equals(role)) newS = new GeneralPractitioner(id, f, l, d, "GP", "N/A");
            else newS = new Specialist(id, f, l, d, "Specialist");

            if(existing == null) controller.addStaff(newS);
            else controller.updateStaff(newS);
            
            refreshTable();
        }
    }

    // --- Custom Gradient Panel for Header ---
    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();
            // Horizontal Gradient from Dark Blue to Light Blue
            GradientPaint gp = new GradientPaint(0, 0, PRIMARY_GRADIENT_1, w, 0, PRIMARY_GRADIENT_2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }
}