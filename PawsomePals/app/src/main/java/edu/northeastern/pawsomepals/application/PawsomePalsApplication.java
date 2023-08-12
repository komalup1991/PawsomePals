package edu.northeastern.pawsomepals.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.google.android.libraries.places.api.Places;

import edu.northeastern.pawsomepals.R;

public class PawsomePalsApplication extends Application {

    public static final String MESSAGE_CHANNEL = "incoming_message";

    private static final String API = "AIzaSyDwYaHCpseu3nk2grk4WpwSNN3jNBQW4eY";

    @Override
    public void onCreate() {
        super.onCreate();
        Places.initialize(getApplicationContext(), API);
        registerNotificationChannel();

    }

    private void registerNotificationChannel() {
        registerNotificationChannelForReceivingMessage();
    }

    private void registerNotificationChannelForReceivingMessage() {
        CharSequence name = getString(R.string.app_name);
        String description = getString(R.string.description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(MESSAGE_CHANNEL, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
}
