package com.hms.model;

public class Nurse extends Staff {
    public Nurse(String staffId, String fName, String lName, String department) {
        super(staffId, fName, lName, "Nurse", department);
    }

    // placeholder actions
    public void administerMedication() { System.out.println("Nurse administering meds..."); }
    public void updatePatientRecord() { System.out.println("Nurse updating record..."); }
}