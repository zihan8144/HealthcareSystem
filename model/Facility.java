package com.hms.model;

public class Facility {
    private String facilityId;
    private String name;
    private String type;
    private String location;

    public Facility(String facilityId, String name, String type, String location) {
        this.facilityId = facilityId;
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public String getName() { return name; }
    
    @Override
    public String toString() { return name + " (" + type + ")"; }
}