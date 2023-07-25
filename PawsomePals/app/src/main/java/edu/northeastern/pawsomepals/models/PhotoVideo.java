package edu.northeastern.pawsomepals.models;

public class PhotoVideo {
    private String createdBy;
    private String caption;
    private String img;
    private String userTagged;
    private String locationTagged;
    private String createdAt;

    public PhotoVideo() {
    }

    public PhotoVideo(String createdBy, String caption, String img, String userTagged, String locationTagged) {
        this.createdBy = createdBy;
        this.caption = caption;
        this.img = img;
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

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
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
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PhotoVideo{" +
                "createdBy='" + createdBy + '\'' +
                ", caption='" + caption + '\'' +
                ", img='" + img + '\'' +
                ", userTagged='" + userTagged + '\'' +
                ", locationTagged='" + locationTagged + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
