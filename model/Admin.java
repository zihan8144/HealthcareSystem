package com.hms.model;

public class Admin extends Staff {
    private String level;

    public Admin(String staffId, String fName, String lName, String department) {
        super(staffId, fName, lName, "Admin", department);
        this.level = "High"; // default access level
    }
    public void manageUsers() { System.out.println("Managing users..."); }
    public void viewAuditLogs() { System.out.println("Viewing logs..."); }
}