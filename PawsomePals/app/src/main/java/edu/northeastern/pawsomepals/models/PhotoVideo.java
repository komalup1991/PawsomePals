package edu.northeastern.pawsomepals.models;

public class PhotoVideo extends FeedItemWithImage  {
    private String caption;

    public PhotoVideo() {
    }

    @Override
    public int getType() {
        return FeedItem.TYPE_PHOTO_VIDEO;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }


}
