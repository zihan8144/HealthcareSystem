package com.hms.model;

import java.util.ArrayList;
import java.util.List;

public class PatientRecord {
    private String recordId;
    private Patient patient;
    private List<String> notes;
    private List<Prescription> prescriptions;
    private List<Referral> referrals;   

    public PatientRecord(String recordId, Patient patient) {
        this.recordId = recordId;
        this.patient = patient;
        // init collections
        this.notes = new ArrayList<>();
        this.prescriptions = new ArrayList<>();
        this.referrals = new ArrayList<>();
    }

    public void addNote(String note) {
        this.notes.add(note);
    }

    public void addPrescription(Prescription p) {
        this.prescriptions.add(p);
    }

    public void addReferral(Referral r) {
        this.referrals.add(r);
    }

    public Patient getPatient() { return patient; }
    public List<Prescription> getPrescriptions() { return prescriptions; }
}