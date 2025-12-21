package com.hms.model;

public class Patient {
    private String patientId;
    private String firstName;
    private String lastName;
    private String dob;
    private String nhsNumber;
    private String gender;
    private String phoneNumber;
    private String email;
    private String address;
    private String postcode;
    private String emergencyName;
    private String emergencyPhone;
    private String regDate;
    private String gpSurgeryId;

    // linked medical record
    private MedicalRecord medicalRecord;

    // init from CSV raw data
    public Patient(String[] data) {
        this.patientId = getSafe(data, 0);
        this.firstName = getSafe(data, 1);
        this.lastName = getSafe(data, 2);
        this.dob = getSafe(data, 3);
        this.nhsNumber = getSafe(data, 4);
        this.gender = getSafe(data, 5);
        this.phoneNumber = getSafe(data, 6);
        this.email = getSafe(data, 7);
        this.address = getSafe(data, 8);
        this.postcode = getSafe(data, 9);
        this.emergencyName = getSafe(data, 10);
        this.emergencyPhone = getSafe(data, 11);
        this.regDate = getSafe(data, 12);
        this.gpSurgeryId = getSafe(data, 13);
        
        this.medicalRecord = new MedicalRecord(this.patientId);
    }

    // GUI Constructor: used when creating a new patient manually
    public Patient(String id, String fName, String lName, String dob, String nhs, String gender, String phone, String gpId) {
        this.patientId = id;
        this.firstName = fName;
        this.lastName = lName;
        this.dob = dob;
        this.nhsNumber = nhs;
        this.gender = gender;
        this.phoneNumber = phone;
        this.gpSurgeryId = gpId;
        
        // set defaults for optional fields
        this.email = ""; 
        this.address = ""; 
        this.postcode = ""; 
        this.medicalRecord = new MedicalRecord(this.patientId);
    }

    // helper to avoid ArrayOutOfBounds
    private String getSafe(String[] data, int index) {
        return (index < data.length) ? data[index].trim() : "";
    }

    public MedicalRecord getMedicalRecord() { return medicalRecord; }
    public String getPatientId() { return patientId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getDob() { return dob; }
    public String getNhsNumber() { return nhsNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getGpSurgeryId() { return gpSurgeryId; }
    public String getRegisteredGpId() { return gpSurgeryId; }
    

    public String toCSV() {
        return String.join(",", patientId, firstName, lastName, dob, nhsNumber, gender, 
               phoneNumber, email, address, postcode, emergencyName, emergencyPhone, regDate, gpSurgeryId);
    }
    
    @Override
    public String toString() { return getFullName() + " (" + patientId + ")"; }
}