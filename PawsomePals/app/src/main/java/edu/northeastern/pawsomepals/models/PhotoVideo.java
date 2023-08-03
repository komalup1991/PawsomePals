package edu.northeastern.pawsomepals.models;

import androidx.annotation.Nullable;

public class PhotoVideo extends FeedItem  {
    private String caption;
    private String img;


    public PhotoVideo() {
    }

    @Override
    public int getType() {
        return FeedItem.TYPE_PHOTO_VIDEO;
    }

    public String getCaption() {
        return caption;
    }

    public String getImg() {
        return img;
    }
    @Override
    public int hashCode() {
        return getFeedItemId().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof PhotoVideo otherPhotoVideo)) {
            return false;
        }

        if (otherPhotoVideo.getFeedItemId() == null || this.getFeedItemId() == null) {
            return false;
        }

        return this.getFeedItemId().equals(otherPhotoVideo.getFeedItemId());
    }


}
