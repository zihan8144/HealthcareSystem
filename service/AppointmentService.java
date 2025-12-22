package com.hms.service;

import com.hms.model.Appointment;
import com.hms.util.CSVHandler;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private List<Appointment> appointments;

    public AppointmentService() {
        this.appointments = new ArrayList<>();
    }

    public void loadData(List<String[]> data) {
        appointments.clear();
        for (String[] row : data) {
            // minimal validation to ensure row has enough columns
            if (row.length >= 10) {
                appointments.add(new Appointment(row));
            }
        }
    }

    public boolean createAppointment(Appointment appt) {
        // basic conflict check: same doc, same date/time
        for (Appointment a : appointments) {
            if (a.getClinicianId().equals(appt.getClinicianId()) && 
                a.getDate().equals(appt.getDate()) && 
                a.getTime().equals(appt.getTime())) {
                return false; // slot taken
            }
        }
        
        // save to memory and file
        appointments.add(appt);
        CSVHandler.appendToCSV("data/appointments.csv", appt.toCSV());
        return true;
    }
    
    public void cancelAppointment(String id) {
        // remove from local list
        appointments.removeIf(a -> a.getAppointmentId().equals(id));
        // sync with CSV
        CSVHandler.deleteRecord("data/appointments.csv", 0, id);
    }
    
    public void updateAppointment(Appointment a) {
        // lazy update: delete old, create new
        cancelAppointment(a.getAppointmentId());
        createAppointment(a);
    }

    public List<Appointment> getAllAppointments() { return appointments; }
}