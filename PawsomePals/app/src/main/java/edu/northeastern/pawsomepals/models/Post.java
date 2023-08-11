package edu.northeastern.pawsomepals.models;

public class Post extends FeedItem {

    private String caption;
    private String postContent;

    public Post() {

    }

    @Override
    public int getType() {
        return FeedItem.TYPE_POST;
    }

    public String getCaption() {
        return caption;
    }

    public String getPostContent() {
        return postContent;
    }


}
