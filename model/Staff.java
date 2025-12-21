package com.hms.model;

public class Staff {
    protected String staffId;
    protected String firstName;
    protected String lastName;
    protected String role;
    protected String department;
    
    // common constructor for all staff types
    public Staff(String id, String fname, String lname, String role, String dept) {
        this.staffId = id;
        this.firstName = fname;
        this.lastName = lname;
        this.role = role;
        this.department = dept;
    }

    public String getStaffId() { return staffId; }
    public String getName() { return firstName + " " + lastName; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    
    @Override
    public String toString() { return getName() + " (" + role + ")"; }
}