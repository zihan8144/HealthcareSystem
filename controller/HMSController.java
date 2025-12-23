package com.hms.controller;

import com.hms.model.*;
import com.hms.service.*;
import com.hms.util.CSVHandler;
import java.util.List;
import java.util.stream.Collectors;

public class HMSController {
    private PatientRecordService patientService;
    private AppointmentService appointmentService;
    private PrescriptionService prescriptionService;
    private ReferralService referralService; // Singleton instance
    private UserManagementService userService;

    private String currentUserId;
    private String currentUserRole;

    public HMSController() {
        // init services
        this.patientService = new PatientRecordService();
        this.appointmentService = new AppointmentService();
        this.prescriptionService = new PrescriptionService();
        this.referralService = ReferralService.getInstance(); 
        this.userService = new UserManagementService();

        loadAllData();
    }

    private void loadAllData() {
        System.out.println("Loading data...");
        // load CSVs
        appointmentService.loadData(CSVHandler.readCSV("data/appointments.csv"));
        prescriptionService.loadData(CSVHandler.readCSV("data/prescriptions.csv"));
        referralService.loadData(CSVHandler.readCSV("data/referrals.csv"));
        userService.loadStaffData();

        // patient data needs linked records
        patientService.loadData(
            CSVHandler.readCSV("data/patients.csv"),
            prescriptionService.getAllPrescriptions(),
            referralService.getAllReferrals()
        );
    }

    public String login(String userId) {
        if (userId == null) return null;
        String id = userId.trim();
        this.currentUserId = id;

        // determine role based on ID prefix
        if (id.startsWith("P")) { this.currentUserRole = "Patient"; return "Patient"; }
        // GP/Specialist/Surgeon but not Student (ST)
        if (id.startsWith("C") || id.startsWith("G") || id.startsWith("S") && !id.startsWith("ST")) { 
            this.currentUserRole = "GP"; return "GP"; 
        } 
        if (id.startsWith("A")) { this.currentUserRole = "Admin"; return "Admin"; }
        if (id.startsWith("R")) { this.currentUserRole = "Receptionist"; return "Receptionist"; }
        if (id.startsWith("N")) { this.currentUserRole = "Nurse"; return "Nurse"; }
        
        // default fallback
        this.currentUserRole = "Staff";
        return "Staff";
    }

    public String getCurrentUserId() { return currentUserId; }
    public String getCurrentUserRole() { return currentUserRole; }

    // --- Data Access Methods ---
    public List<Patient> getPatients() { 
        // Patients only see their own record; Staff sees everyone (for booking)
        if ("Patient".equals(currentUserRole)) {
            return patientService.getPatients().stream()
                .filter(p -> p.getPatientId().equalsIgnoreCase(currentUserId))
                .collect(Collectors.toList());
        }
        return patientService.getPatients(); 
    }
    
    public List<Staff> getStaffMembers() { return userService.getAllStaff(); }
    
    public List<Appointment> getAppointments() {
        List<Appointment> all = appointmentService.getAllAppointments();
        
        // filter by role
        if ("Patient".equals(currentUserRole)) {
            return all.stream().filter(a -> a.getPatientId().equalsIgnoreCase(currentUserId)).collect(Collectors.toList());
        } else if ("GP".equals(currentUserRole) || "Specialist".equals(currentUserRole)) {
            return all.stream().filter(a -> a.getClinicianId().equalsIgnoreCase(currentUserId)).collect(Collectors.toList());
        }
        return all;
    }

     public List<Prescription> getPrescriptions() {
        List<Prescription> all = prescriptionService.getAllPrescriptions();
        // filter: patient sees own, doc sees issued ones
        if ("Patient".equals(currentUserRole)) {
            return all.stream().filter(p -> p.getPatientId().equalsIgnoreCase(currentUserId)).collect(Collectors.toList());
        } else if ("GP".equals(currentUserRole) || "Specialist".equals(currentUserRole)) {
            return all.stream().filter(p -> p.getClinicianId().equalsIgnoreCase(currentUserId)).collect(Collectors.toList());
        }
        return all;
    }

    public List<Referral> getReferrals() {
        List<Referral> all = referralService.getAllReferrals();
        if ("Patient".equals(currentUserRole)) {
            return all.stream().filter(r -> r.getPatientId().equalsIgnoreCase(currentUserId)).collect(Collectors.toList());
        } else if ("GP".equals(currentUserRole) || "Specialist".equals(currentUserRole)) {
            // check both referring and referred-to IDs
            return all.stream().filter(r -> r.getReferringClinicianId().equalsIgnoreCase(currentUserId) || 
                                            r.getReferredToClinicianId().equalsIgnoreCase(currentUserId))
                      .collect(Collectors.toList());
        }
        return all;
    }
    public List<String> getAuditLogs() { return userService.getAuditLogs(); }

    // --- Action Methods ---
    public void addAppointment(Appointment a) { appointmentService.createAppointment(a); }
    public void updateAppointment(Appointment a) { appointmentService.updateAppointment(a); }
    public void deleteAppointment(String id) { appointmentService.cancelAppointment(id); }

    public void addPrescription(Prescription p) { prescriptionService.createPrescription(p); }
    public void updatePrescription(Prescription p) { prescriptionService.updatePrescription(p); }
    public void deletePrescription(String id) { prescriptionService.deletePrescription(id); }
    public void requestRefill(Prescription p) { prescriptionService.requestRefill(p); }

    public void addReferral(Referral r) { referralService.createReferral(r); }
    public void updateReferral(Referral r) { referralService.updateReferral(r); }
    public void deleteReferral(String id) { referralService.deleteReferral(id); }

    public void addPatient(Patient p) { patientService.registerPatient(p); }
    public void updatePatient(Patient p) { patientService.updatePatient(p); }
    public void deletePatient(String id) { patientService.deletePatient(id); }
    
    // --- Staff Management ---
    public void addStaff(Staff s) { 
        userService.addStaff(s); 
    }
    
    public void updateStaff(Staff s) { 
        userService.updateStaff(s); 
    }
    
    public void deleteStaff(String id) { 
        userService.deleteStaff(id); 
    }

    public Patient getCurrentPatient() {
        if (!"Patient".equals(currentUserRole)) return null;
        for (Patient p : patientService.getPatients()) {
            if (p.getPatientId().equalsIgnoreCase(currentUserId)) return p;
        }
        return null;
    }

    public Staff getCurrentStaff() {
        for (Staff s : userService.getAllStaff()) {
            if (s.getStaffId().equalsIgnoreCase(currentUserId)) return s;
        }
        return null;
    }
    
    public void printPrescription(Prescription p) {
        // 1. generate file via service
        prescriptionService.exportToTextFile(p);
        // 2. log the action
        userService.logAction(currentUserId, "Printed Prescription to File: " + p.getPrescriptionId());
    }
    
    public void printPrescriptionToFile(Prescription p) {
        String filename = "data/Prescription_" + p.getPrescriptionId() + ".txt";
        StringBuilder content = new StringBuilder();
        content.append("========== PRESCRIPTION ==========\n");
        content.append("Prescription ID: ").append(p.getPrescriptionId()).append("\n");
        // TODO: should use actual date from 'p'
        content.append("Date: ").append("2024-XX-XX").append("\n"); 
        content.append("----------------------------------\n");
        content.append("Patient ID: ").append(p.getPatientId()).append("\n");
        content.append("Doctor ID:  ").append(p.getClinicianId()).append("\n");
        content.append("----------------------------------\n");
        content.append("Medication: ").append(p.getMedication()).append("\n");
        content.append("Dosage:     ").append(p.getDosage()).append("\n");
        content.append("Status:     ").append(p.getStatus()).append("\n");
        content.append("==================================\n");
        
        // reuse CSV handler for appending
        CSVHandler.appendToCSV(filename, content.toString()); 
        userService.logAction(currentUserId, "Printed Prescription: " + filename);
        System.out.println("Prescription printed to " + filename);
    }

    // --- New Feature: Generate Referral Letter (simulates email) ---
    public void generateReferralLetter(Referral r) {
        String filename = "data/Referral_Letter_" + r.getReferralId() + ".txt";
        StringBuilder content = new StringBuilder();
        content.append("========== REFERRAL LETTER ==========\n");
        content.append("URGENCY: ").append(r.getUrgency().toUpperCase()).append("\n");
        content.append("From: Dr. ").append(r.getReferringClinicianId()).append("\n");
        content.append("To:   Dr. ").append(r.getReferredToClinicianId()).append("\n");
        content.append("Date: ").append("2024-XX-XX").append("\n");
        content.append("-------------------------------------\n");
        content.append("Re: Patient ID ").append(r.getPatientId()).append("\n");
        content.append("\n");
        content.append("Clinical Summary:\n");
        content.append(r.getSummary()).append("\n");
        content.append("\n");
        content.append("Status: ").append(r.getStatus()).append("\n");
        content.append("=====================================\n");

        CSVHandler.appendToCSV(filename, content.toString());
        userService.logAction(currentUserId, "Generated Referral Letter: " + filename);
        System.out.println("Referral letter generated: " + filename);
    }
}