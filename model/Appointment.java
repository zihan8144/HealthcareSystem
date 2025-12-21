package com.hms.model;

public class Appointment {
    private String appointmentId;
    private String patientId;
    private String clinicianId;
    private String facilityId;
    private String date;
    private String time;
    private String duration;
    private String type;
    private String status;
    private String reason;
    private String notes;

    public Appointment(String[] data) {
        this.appointmentId = getSafe(data, 0);
        this.patientId = getSafe(data, 1);
        this.clinicianId = getSafe(data, 2);
        this.facilityId = getSafe(data, 3);
        this.date = getSafe(data, 4);
        this.time = getSafe(data, 5);
        this.duration = getSafe(data, 6);
        this.type = getSafe(data, 7);
        this.status = getSafe(data, 8);
        this.reason = getSafe(data, 9);
        this.notes = getSafe(data, 10);
    }
    
    // GUI Constructor
    public Appointment(String id, String pid, String cid, String date, String time, String reason) {
        this.appointmentId = id;
        this.patientId = pid;
        this.clinicianId = cid;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.status = "Scheduled";
        this.facilityId = "FAC001"; // Default
        this.duration = "15";
        this.type = "General";
        this.notes = "";
    }

    private String getSafe(String[] data, int index) {
        return (index < data.length) ? data[index].trim() : "";
    }

    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getClinicianId() { return clinicianId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }

    public String toCSV() {
        // Use placeholders for create/modify dates to match column count
        return String.join(",", appointmentId, patientId, clinicianId, facilityId, date, time, 
                           duration, type, status, reason, notes, "2024-01-01", "2024-01-01");
    }
}