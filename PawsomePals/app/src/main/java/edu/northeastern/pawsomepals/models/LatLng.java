package edu.northeastern.pawsomepals.models;

import java.io.Serializable;

public class LatLng implements Serializable {
    private double latitude;
    private double longitude;

    public LatLng() {

    }

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}