package edu.northeastern.pawsomepals.models;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class ChatLocationModel {
    private String locationName;
    private String locationAddress;
    private double latitude;
    private double longitude;

    public ChatLocationModel(String locationName, String locationAddress, double latitude,double longitude) {
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ChatLocationModel() {
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return this.locationName + "\n" + this.locationAddress;
    }
}
