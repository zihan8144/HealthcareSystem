package com.hms.model;

public class Prescription {
    private String prescriptionId;
    private String patientId;
    private String clinicianId;
    private String medication;
    private String dosage;
    private String status;
    
    // Additional fields from CSV
    private String apptId;
    private String date;
    private String frequency;
    private String duration;

    public Prescription(String[] data) {
        this.prescriptionId = getSafe(data, 0);
        this.patientId = getSafe(data, 1);
        this.clinicianId = getSafe(data, 2);
        this.apptId = getSafe(data, 3);
        this.date = getSafe(data, 4);
        this.medication = getSafe(data, 5);
        this.dosage = getSafe(data, 6);
        this.frequency = getSafe(data, 7);
        this.duration = getSafe(data, 8);
        this.status = getSafe(data, 12); // Index 12 is status
    }
    
    // GUI Constructor
    public Prescription(String id, String pid, String cid, String med, String dose, String status) {
        this.prescriptionId = id;
        this.patientId = pid;
        this.clinicianId = cid;
        this.medication = med;
        this.dosage = dose;
        this.status = status;
        
        // hardcode defaults for now
        this.date = "2024-01-01";
        this.apptId = "N/A";
        this.frequency = "Daily";
        this.duration = "7";
    }

    private String getSafe(String[] data, int index) {
        return (index < data.length) ? data[index].trim() : "";
    }

    public String getPrescriptionId() { return prescriptionId; }
    public String getPatientId() { return patientId; }
    public String getClinicianId() { return clinicianId; }
    public String getMedication() { return medication; }
    public String getDosage() { return dosage; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }

    public String toCSV() {
        return String.join(",", prescriptionId, patientId, clinicianId, apptId, date, medication, 
                           dosage, frequency, duration, "1", "Instructions", "Pharmacy", status, date);
    }
}