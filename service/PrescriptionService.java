package com.hms.service;

import com.hms.model.Prescription;
import com.hms.util.CSVHandler;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionService {
    private List<Prescription> prescriptions;

    public PrescriptionService() {
        this.prescriptions = new ArrayList<>();
    }

    public void loadData(List<String[]> data) {
        prescriptions.clear();
        for (String[] row : data) {
            if (row.length >= 10) {
                prescriptions.add(new Prescription(row));
            }
        }
    }

    public void createPrescription(Prescription p) {
        prescriptions.add(p);
        CSVHandler.appendToCSV("data/prescriptions.csv", p.toCSV());
    }
    
    public void deletePrescription(String id) {
        prescriptions.removeIf(p -> p.getPrescriptionId().equals(id));
        CSVHandler.deleteRecord("data/prescriptions.csv", 0, id);
    }
    
    public void updatePrescription(Prescription p) {
        // simple update: remove old, add new
        deletePrescription(p.getPrescriptionId());
        createPrescription(p);
    }
    
    public void requestRefill(Prescription p) {
        deletePrescription(p.getPrescriptionId());
        p.setStatus("Refill Requested");
        createPrescription(p);
    }

    // simulates printing to a physical file
    public void exportToTextFile(Prescription p) {
        String filename = "data/Prescription_" + p.getPrescriptionId() + ".txt";
        // try-with-resources for auto-close
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("========== PRESCRIPTION DOCUMENT ==========");
            bw.newLine();
            bw.write("Prescription ID: " + p.getPrescriptionId());
            bw.newLine();
            bw.write("Date:            2024-XX-XX"); // TODO: use actual date
            bw.newLine();
            bw.write("-------------------------------------------");
            bw.newLine();
            bw.write("Patient ID:      " + p.getPatientId());
            bw.newLine();
            bw.write("Clinician ID:    " + p.getClinicianId());
            bw.newLine();
            bw.write("-------------------------------------------");
            bw.newLine();
            bw.write("Medication:      " + p.getMedication());
            bw.newLine();
            bw.write("Dosage:          " + p.getDosage());
            bw.newLine();
            bw.write("Status:          " + p.getStatus());
            bw.newLine();
            bw.write("===========================================");
            bw.newLine();
            System.out.println("File generated: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Prescription> getAllPrescriptions() { return prescriptions; }
}