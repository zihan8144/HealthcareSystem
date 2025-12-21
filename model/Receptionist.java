package com.hms.model;

public class Receptionist extends Staff {
    private String desk; 

    public Receptionist(String staffId, String fName, String lName, String department) {
        super(staffId, fName, lName, "Receptionist", department);
        this.desk = "Front Desk"; // default desk assignment
    }

    // common receptionist tasks
    public void registerPatient() { System.out.println("Registering patient..."); }
    public void createAppointment() { System.out.println("Creating appointment..."); }
    public void modifyAppointment() { System.out.println("Modifying appointment..."); }
    public void cancelAppointment() { System.out.println("Cancelling appointment..."); }
    public void manageAppointments() { System.out.println("Managing appointments..."); }
}