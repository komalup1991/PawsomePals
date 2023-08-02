package edu.northeastern.pawsomepals.models;

public class Post extends FeedItem  {
    private String postId;
    private String caption;
    private String postContent;

    public Post() {

    }

    public Post(String username, String userProfileImage, String createdAt, String userTagged, String locationTagged, String createdBy, String postId, String caption, String postContent,Long commentCount) {
        super(username, userProfileImage, createdAt, userTagged, locationTagged, createdBy,commentCount);
        this.postId = postId;
        this.caption = caption;
        this.postContent = postContent;
    }

    @Override
    public int getType() {
        return FeedItem.TYPE_POST;
    }

    public String getPostId() {
        return postId;
    }

    public String getCaption() {
        return caption;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }


}
