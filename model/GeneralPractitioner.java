package com.hms.model;

public class GeneralPractitioner extends Staff {
    private String specialty;
    private String licenseNumber; 

    // updated constructor: split name into first/last
    public GeneralPractitioner(String staffId, String fName, String lName, String department, String specialty, String licenseNumber) {
        super(staffId, fName, lName, "GP", department);
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
    }

    // placeholder methods for future implementation
    public void createPrescription() { System.out.println("GP creating prescription..."); }
    public void updatePatientRecord() { System.out.println("GP updating record..."); }
    public void initiateReferral() { System.out.println("GP initiating referral..."); }
    public void modifyAppointment() { System.out.println("GP modifying appointment..."); }

    public String getSpecialty() { return specialty; }
}