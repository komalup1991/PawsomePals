package edu.northeastern.pawsomepals.models;

import java.io.Serializable;

public abstract class FeedItem  implements Comparable<FeedItem>, Serializable {

    public static final int TYPE_RECIPE_HEADER = 0;
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
    private String feedItemId;
    private Long commentCount;
    private Long likeCount;

    private boolean isFavorite;
    private boolean isLiked;

    private LatLng latLng;

    public FeedItem() {
    }

    public FeedItem(String username, String userProfileImage, String createdAt, String userTagged, String locationTagged, String createdBy, Long commentCount, Long likeCount) {
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

    public String getFeedItemId() {
        return feedItemId;
    }

    public void setFeedItemId(String feedItemId) {
        this.feedItemId = feedItemId;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    @Override
    public int compareTo(FeedItem feedItem) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
//
//        try {
//            Date thisDate = sdf.parse(this.getCreatedAt());
//            Date otherDate = sdf.parse(feedItem.getCreatedAt());
//
//            // Compare the dates and return the result
//            return thisDate.compareTo(otherDate);
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
        return 0;
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
}


//    private Date convertStringToDate(String createdAt) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
//        try {
//            return sdf.parse(createdAt);
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
//    }


