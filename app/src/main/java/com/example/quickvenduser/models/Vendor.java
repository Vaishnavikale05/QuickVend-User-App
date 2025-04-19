package com.example.quickvenduser.models;
public class Vendor {
    private String name;
    private String location;
    private double latitude;
    private double longitude;

    public Vendor() {}

    public Vendor(String name, String location, double latitude, double longitude) {
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
