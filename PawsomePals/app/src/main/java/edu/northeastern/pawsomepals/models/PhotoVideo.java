package edu.northeastern.pawsomepals.models;

public class PhotoVideo extends FeedItem  {
    private String caption;
    private String img;
    private String photoVideoId;

    public PhotoVideo() {
    }

    public PhotoVideo(String username, String userProfileImage, String createdAt, String userTagged, String locationTagged, String createdBy, String caption, String img) {
        super(username, userProfileImage, createdAt, userTagged, locationTagged, createdBy);
        this.caption = caption;
        this.img = img;
    }

    @Override
    public int getType() {
        return FeedItem.TYPE_PHOTO_VIDEO;
    }

    public String getCaption() {
        return caption;
    }

    public String getPhotoVideoId() {
        return photoVideoId;
    }

    public String getImg() {
        return img;
    }

    public void setPhotoVideoId(String photoVideoId) {
        this.photoVideoId = photoVideoId;
    }


}
