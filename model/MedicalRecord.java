package com.hms.model;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecord {
    private String patientId;
    // linked records

    private List<Prescription> prescriptions;
    private List<Referral> referrals;
    private List<String> clinicalNotes;

    
    public MedicalRecord(String patientId) {
        this.patientId = patientId;
        this.prescriptions = new ArrayList<>();
        this.referrals = new ArrayList<>();
        this.clinicalNotes = new ArrayList<>();
    }

    public void addPrescription(Prescription p) {
        this.prescriptions.add(p);
    }

    public void addReferral(Referral r) {
        this.referrals.add(r);
    }

    public void addNote(String note) {
        this.clinicalNotes.add(note);
    }

    public List<Prescription> getPrescriptions() { return prescriptions; }
    public List<Referral> getReferrals() { return referrals; }
    public List<String> getClinicalNotes() { return clinicalNotes; }
}