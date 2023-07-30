package edu.northeastern.pawsomepals.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    public static String formatTime(String timeCreated) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timeAgoText = null;

        Date firebaseTimestamp = dateFormat.parse(timeCreated);
        long currentTimeMillis = System.currentTimeMillis();
        long firebaseTimeMillis = firebaseTimestamp.getTime();
        long timeDifferenceMillis = currentTimeMillis - firebaseTimeMillis;

        if (timeDifferenceMillis < 0) {
            timeAgoText = "Future date";
        } else if (timeDifferenceMillis < 60 * 1000) { // Less than 1 minute
            timeAgoText = "Just now";
        } else if (timeDifferenceMillis < 60 * 60 * 1000) { // Less than 1 hour
            int minutes = (int) (timeDifferenceMillis / (60 * 1000));
            if (minutes == 1)
                timeAgoText = minutes + " minute ago";
            else
                timeAgoText = minutes + " minutes ago";
        } else if (timeDifferenceMillis < 24 * 60 * 60 * 1000) { // Less than 1 day
            int hours = (int) (timeDifferenceMillis / (60 * 60 * 1000));
            if (hours == 1)
                timeAgoText = hours + " hour ago";
            else
                timeAgoText = hours + " hours ago";
        } else {
            int days = (int) (timeDifferenceMillis / (24 * 60 * 60 * 1000));
            timeAgoText = days + " days ago";
        }

        return timeAgoText;
    }
}
