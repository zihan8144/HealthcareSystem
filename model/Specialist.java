package com.hms.model;

public class Specialist extends Staff {
    private String specialty;

    public Specialist(String staffId, String fName, String lName, String department, String specialty) {
        super(staffId, fName, lName, "Specialist", department);
        this.specialty = specialty;
    }

    // placeholder actions for specialists
    public void viewReferral() { System.out.println("Specialist viewing referral..."); }
    public void createPrescription() { System.out.println("Specialist creating prescription..."); }
    public void updatePatientRecord() { System.out.println("Specialist updating record..."); }
    
    public String getSpecialty() { return specialty; }
}