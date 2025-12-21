package com.hms.model;

public class Referral {
    private String referralId;
    private String patientId;
    private String referringClinicianId;
    private String referredToClinicianId;
    private String date;
    private String urgency;
    private String summary;
    private String status;

    public Referral(String[] data) {
        this.referralId = getSafe(data, 0);
        this.patientId = getSafe(data, 1);
        this.referringClinicianId = getSafe(data, 2);
        this.referredToClinicianId = getSafe(data, 3);
        // skipping facility IDs at index 4, 5
        this.date = getSafe(data, 6);
        this.urgency = getSafe(data, 7);
        // skipping reason at index 8
        this.summary = getSafe(data, 9);
        this.status = getSafe(data, 11);
    }
    
    // GUI Constructor (for new referrals)
    public Referral(String id, String pid, String fromId, String toId, String urgency, String summary) {
        this.referralId = id;
        this.patientId = pid;
        this.referringClinicianId = fromId;
        this.referredToClinicianId = toId;
        this.urgency = urgency;
        this.summary = summary;
        
        // set defaults for new referrals
        this.status = "Pending";
        this.date = "2024-01-01";
    }

    // safe array access
    private String getSafe(String[] data, int index) {
        return (index < data.length) ? data[index].trim() : "";
    }

    public String getReferralId() { return referralId; }
    public String getPatientId() { return patientId; }
    public String getReferringClinicianId() { return referringClinicianId; }
    public String getReferredToClinicianId() { return referredToClinicianId; }
    public String getUrgency() { return urgency; }
    public String getSummary() { return summary; }
    public String getStatus() { return status; }

    public String toCSV() {
        // CSV structure likely requires more fields, fill with placeholders
        return String.join(",", referralId, patientId, referringClinicianId, referredToClinicianId, 
               "FAC001", "FAC002", date, urgency, "Reason", summary, "None", status, "N/A", "Notes", date, date);
    }
}