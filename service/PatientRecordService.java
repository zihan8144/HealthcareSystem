package com.hms.service;

import com.hms.model.*;
import com.hms.util.CSVHandler;
import java.util.ArrayList;
import java.util.List;

public class PatientRecordService {
    private List<Patient> patients;

    public PatientRecordService() {
        this.patients = new ArrayList<>();
    }

    // Key: load patient data and link related records (Prescriptions, Referrals)
    public void loadData(List<String[]> patientData, List<Prescription> allPrescriptions, List<Referral> allReferrals) {
        patients.clear();
        for (String[] row : patientData) {
            // sanity check: ensure valid row
            if (row.length >= 10) { 
                Patient p = new Patient(row);
                
                // populate MedicalRecord
                MedicalRecord record = p.getMedicalRecord();
                
                // 1. link prescriptions to this patient
                for (Prescription rx : allPrescriptions) {
                    if (rx.getPatientId().equals(p.getPatientId())) {
                        record.addPrescription(rx);
                    }
                }
                
                // 2. link referrals to this patient
                for (Referral ref : allReferrals) {
                    if (ref.getPatientId().equals(p.getPatientId())) {
                        record.addReferral(ref);
                    }
                }
                
                patients.add(p);
            }
        }
        System.out.println("Loaded " + patients.size() + " patients.");
    }

    public void registerPatient(Patient p) {
        patients.add(p);
        CSVHandler.appendToCSV("data/patients.csv", p.toCSV());
    }
    
    public void deletePatient(String id) {
        patients.removeIf(p -> p.getPatientId().equals(id));
        CSVHandler.deleteRecord("data/patients.csv", 0, id);
    }
    
    public void updatePatient(Patient p) {
        deletePatient(p.getPatientId());
        registerPatient(p);
    }

    public List<Patient> getPatients() { return patients; }
}