package edu.northeastern.pawsomepals.models;

import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public abstract class FeedItem  implements Comparable<FeedItem> {
    public static final int TYPE_PHOTO_VIDEO = 1;
    public static final int TYPE_SERVICE = 2;
    public static final int TYPE_EVENT = 3;
    public static final int TYPE_POST = 4;
    private String username;
    private String userProfileImage;
    private String createdAt;
    private String userTagged;
    private String locationTagged;
    private String createdBy;

    public FeedItem() {}

    public FeedItem(String username, String userProfileImage, String createdAt, String userTagged, String locationTagged, String createdBy) {
        this.username = username;
        this.userProfileImage = userProfileImage;
        this.createdAt = createdAt;
        this.userTagged = userTagged;
        this.locationTagged = locationTagged;
        this.createdBy = createdBy;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUserTagged() {
        return userTagged;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLocationTagged() {
        return locationTagged;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    abstract public int getType();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    @Override
    public int compareTo(FeedItem feedItem) {
//        Log.d("yoo", "convertStringToDate(f eedItem2.getCreatedAt()) = " + convertStringToDate(feedItem2.getCreatedAt()));
//        Log.d("yoo", "convertStringToDate(feedItem.getCreatedAt()) = " + convertStringToDate(feedItem.getCreatedAt()));
//        return convertStringToDate(this.getCreatedAt())
//                .compareTo(convertStringToDate(feedItem.getCreatedAt()));
        return 0;
    }

//    private Date convertStringToDate(String createdAt) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
//        try {
//            return sdf.parse(createdAt);
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
