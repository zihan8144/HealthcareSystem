package com.hms.service;

import com.hms.model.*;
import com.hms.util.CSVHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserManagementService {
    private List<Staff> staffMembers;
    private List<String> auditLogs;

    public UserManagementService() {
        this.staffMembers = new ArrayList<>();
        this.auditLogs = new ArrayList<>();
        loadAuditLogs(); 
    }

    private void loadAuditLogs() {
        File file = new File("data/audit_logs.txt");
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) auditLogs.add(line);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void loadStaffData() {
        staffMembers.clear();

        // 1. Load Staff.csv (staff_id, first_name, last_name, role, department...)
        List<String[]> staffData = CSVHandler.readCSV("data/staff.csv");
        for (String[] row : staffData) {
            if (row.length >= 5) {
                String id = row[0];
                String fname = row[1];
                String lname = row[2];
                String role = row[3];
                String dept = row[4];
                
                // instantiate concrete class based on role
                if(role.equalsIgnoreCase("Admin")) staffMembers.add(new Admin(id, fname, lname, dept));
                else if(role.equalsIgnoreCase("Receptionist")) staffMembers.add(new Receptionist(id, fname, lname, dept));
                else if(role.equalsIgnoreCase("Nurse")) staffMembers.add(new Nurse(id, fname, lname, dept));
                else staffMembers.add(new Staff(id, fname, lname, role, dept));
            }
        }

        // 2. Load Clinicians.csv (clinician_id, first_name, last_name, title, speciality...)
        List<String[]> clinicianData = CSVHandler.readCSV("data/clinicians.csv");
        for (String[] row : clinicianData) {
            if (row.length >= 5) {
                String id = row[0];
                String fname = row[1];
                String lname = row[2];
                String title = row[3];     // e.g. Dr. / Mr.
                String specialty = row[4]; // e.g. Cardiology
                
                // distinguish between GP and Specialist
                if (specialty.contains("General") || specialty.contains("GP")) {
                    staffMembers.add(new GeneralPractitioner(id, fname, lname, "Clinical", specialty, "GMC-XXX"));
                } else {
                    staffMembers.add(new Specialist(id, fname, lname, "Clinical", specialty));
                }
            }
        }
    }

    public List<Staff> getAllStaff() { return staffMembers; }

    public void addStaff(Staff s) {
        staffMembers.add(s);
        // simple name splitting
        String[] parts = s.getName().split(" ", 2);
        String f = parts[0]; String l = (parts.length > 1) ? parts[1] : "";
        
        // determine which CSV to write to based on type
        if (s instanceof GeneralPractitioner || s instanceof Specialist) {
            String spec = (s instanceof GeneralPractitioner) ? ((GeneralPractitioner)s).getSpecialty() : ((Specialist)s).getSpecialty();
            // format: id, fname, lname, title, speciality...
            String line = String.join(",", s.getStaffId(), f, l, "Dr.", spec, "N/A", "N/A", "email", s.getDepartment(), "Hosp", "Active", "2024");
            CSVHandler.appendToCSV("data/clinicians.csv", line);
        } else {
            // format: id, fname, lname, role, dept...
            String line = String.join(",", s.getStaffId(), f, l, s.getRole(), s.getDepartment(), "FAC001", "N/A", "email", "Active", "2024", "N/A", "1");
            CSVHandler.appendToCSV("data/staff.csv", line);
        }
        logAction("Admin", "Added/Updated Staff: " + s.getStaffId());
    }

    // --- New Feature: Delete Staff ---
    public void deleteStaff(String id) {
        staffMembers.removeIf(s -> s.getStaffId().equals(id));
        // try deleting from both files to be safe
        CSVHandler.deleteRecord("data/staff.csv", 0, id);
        CSVHandler.deleteRecord("data/clinicians.csv", 0, id);
        logAction("Admin", "Deleted staff: " + id);
    }

    // --- New Feature: Update Staff ---
    public void updateStaff(Staff s) {
        deleteStaff(s.getStaffId()); // delete first
        addStaff(s);                 // then re-add
        System.out.println("Staff updated: " + s.getName());
    }

    public void logAction(String user, String action) {
        String logEntry = "[User: " + user + "] " + action;
        auditLogs.add(logEntry);
        // persist to file immediately
        CSVHandler.appendToCSV("data/audit_logs.txt", logEntry);
    }

    public List<String> getAuditLogs() { return auditLogs; }
}