package edu.northeastern.pawsomepals.models;

public abstract class FeedItem   {
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
}
