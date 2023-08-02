package edu.northeastern.pawsomepals.models;

import androidx.annotation.Nullable;

public class Post extends FeedItem  {

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
    @Override
    public int hashCode() {
        return getFeedItemId().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Post otherPost)) {
            return false;
        }

        if (otherPost.getFeedItemId() == null || this.getFeedItemId() == null) {
            return false;
        }

        return this.getFeedItemId().equals(otherPost.getFeedItemId());
    }



}
