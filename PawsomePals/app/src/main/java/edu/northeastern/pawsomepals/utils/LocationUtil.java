package edu.northeastern.pawsomepals.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtil {
    private static final int REQUEST_CODE_LOCATION_PERMISSIONS = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private Activity activity;
    private Context context;
    private Geocoder geocoder;
    private TextView locationTaggedTextView;



    public LocationUtil(Activity activity, TextView locationTaggedTextView) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.locationTaggedTextView = locationTaggedTextView;
        this.geocoder = new Geocoder(context, Locale.getDefault());
        setButtonClickListener();
    }

    private void setButtonClickListener() {
        locationTaggedTextView.setOnClickListener(v -> requestLocationPermission());
    }

    private boolean checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

//    private void requestLocationPermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ActivityCompat.requestPermissions(activity,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
//                    REQUEST_CODE_LOCATION_PERMISSIONS);
//        } else {
//            ActivityCompat.requestPermissions(activity,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                    REQUEST_CODE_LOCATION_PERMISSIONS);
//        }
//    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationName();
            //do something
        } else{
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }



    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLocationName();
            } else {
                Toast.makeText(context, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocationName() {
        if (checkLocationPermissions()) {
            try {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();

                                    String locationName = getLocationNameFromCoordinates(latitude, longitude);
                                    if (locationName != null) {
                                        locationTaggedTextView.setText(locationName);
                                       // Toast.makeText(context, "Location Name: " + locationName, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Location Name not found.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(context, "Location data is not available.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            requestLocationPermission();
        }
    }



    private void handlePermissionDenied() {
        Toast.makeText(activity, "Location permission denied", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    private String getLocationNameFromCoordinates(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder addressBuilder = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressBuilder.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        addressBuilder.append(", ");
                    }
                }
                return addressBuilder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
