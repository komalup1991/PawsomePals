package edu.northeastern.pawsomepals.models;

import java.io.Serializable;

public class LatLng implements Serializable {
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}