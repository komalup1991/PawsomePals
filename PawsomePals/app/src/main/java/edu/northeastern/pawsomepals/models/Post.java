package edu.northeastern.pawsomepals.models;

public class Post {
    private String createdBy;
    private String caption;
    private String postContent;
    private String userTagged;
    private String locationTagged;

    public Post() {
    }

    public Post(String createdBy, String caption, String postContent, String userTagged, String locationTagged) {
        this.createdBy = createdBy;
        this.caption = caption;
        this.postContent = postContent;
        this.userTagged = userTagged;
        this.locationTagged = locationTagged;
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

    @Override
    public String toString() {
        return "Post{" +
                "createdBy='" + createdBy + '\'' +
                ", caption='" + caption + '\'' +
                ", postContent='" + postContent + '\'' +
                ", userTagged='" + userTagged + '\'' +
                ", locationTagged='" + locationTagged + '\'' +
                '}';
    }
}
