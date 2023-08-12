package edu.northeastern.pawsomepals.models;

import java.io.Serializable;
import java.util.Objects;

public abstract class FeedItem implements Serializable {

    public static final int TYPE_RECIPE_HEADER = 0;
    public static final int TYPE_PHOTO_VIDEO = 1;
    public static final int TYPE_SERVICE = 2;
    public static final int TYPE_EVENT = 3;
    public static final int TYPE_POST = 4;

    public static final int TYPE_RECIPE = 5;

    private String username;
    private String userProfileImage;
    private String createdAt;
    private String userTagged;
    private String locationTagged;
    private String createdBy;
    private String feedItemId;
    private long commentCount;
    private long likeCount;
    private boolean isFavorite;
    private boolean isLiked;

    private LatLng latLng;

    private String displayTime;

    private int type;

    public FeedItem() {
    }

    public FeedItem(String username, String userProfileImage, String createdAt, String userTagged, String locationTagged, String createdBy, long commentCount, Long likeCount) {
        this.username = username;
        this.userProfileImage = userProfileImage;
        this.createdAt = createdAt;
        this.userTagged = userTagged;
        this.locationTagged = locationTagged;
        this.createdBy = createdBy;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
    }


    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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

    public String getFeedItemId() {
        return feedItemId;
    }

    public void setFeedItemId(String feedItemId) {
        this.feedItemId = feedItemId;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedItem feedItem = (FeedItem) o;
        return commentCount == feedItem.commentCount && likeCount == feedItem.likeCount
                && isFavorite == feedItem.isFavorite && isLiked == feedItem.isLiked
                && type == feedItem.type && username.equals(feedItem.username)
                && userProfileImage.equals(feedItem.userProfileImage)
                && createdAt.equals(feedItem.createdAt)
                && Objects.equals(userTagged, feedItem.userTagged)
                && Objects.equals(locationTagged, feedItem.locationTagged)
                && createdBy.equals(feedItem.createdBy)
                && feedItemId.equals(feedItem.feedItemId)
                && Objects.equals(latLng, feedItem.latLng)
                && Objects.equals(displayTime, feedItem.displayTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, userProfileImage, createdAt, userTagged, locationTagged, createdBy, feedItemId, commentCount, likeCount, isFavorite, isLiked, latLng, displayTime, type);
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUserTagged(String userTagged) {
        this.userTagged = userTagged;
    }

    public void setLocationTagged(String locationTagged) {
        this.locationTagged = locationTagged;
    }
}
