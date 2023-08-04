package edu.northeastern.pawsomepals.application;

import android.app.Application;

import com.google.android.libraries.places.api.Places;

public class PawsomePalsApplication extends Application {

    private static final String API = "AIzaSyDwYaHCpseu3nk2grk4WpwSNN3jNBQW4eY";

    @Override
    public void onCreate() {
        super.onCreate();
        Places.initialize(getApplicationContext(), API);
    }
}
