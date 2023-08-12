package edu.northeastern.pawsomepals.models;

public abstract class FeedItemWithImage extends FeedItem {
    private String img;

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
