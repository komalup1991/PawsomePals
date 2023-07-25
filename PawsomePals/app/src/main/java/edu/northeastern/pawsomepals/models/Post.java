package edu.northeastern.pawsomepals.models;

public class Post {
    private String postId;
    private String createdBy;
    private String caption;
    private String postContent;
    private String userTagged;
    private String locationTagged;
    private String username;
    private String userProfileImage;
    private String createdAt;


    public Post() {
    }

    public Post(String postId, String createdBy, String caption, String postContent, String userTagged, String locationTagged) {
        this.postId = postId;
        this.createdBy = createdBy;
        this.caption = caption;
        this.postContent = postContent;
        this.userTagged = userTagged;
        this.locationTagged = locationTagged;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getUserTagged() {
        return userTagged;
    }

    public void setUserTagged(String userTagged) {
        this.userTagged = userTagged;
    }

    public String getLocationTagged() {
        return locationTagged;
    }

    public void setLocationTagged(String locationTagged) {
        this.locationTagged = locationTagged;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", caption='" + caption + '\'' +
                ", postContent='" + postContent + '\'' +
                ", userTagged='" + userTagged + '\'' +
                ", locationTagged='" + locationTagged + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }

}
