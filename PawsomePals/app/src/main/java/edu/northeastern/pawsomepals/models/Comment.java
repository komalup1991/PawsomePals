package edu.northeastern.pawsomepals.models;

public class Comment {
    private String comment;
    private String createdBy;
    private String commentId;
    private String createdAt;
    private String username;
    private String userProfileImage;

    public Comment() {
    }

    public Comment(String comment, String createdBy, String commentId, String createdAt, String username, String userProfileImage) {
        this.comment = comment;
        this.createdBy = createdBy;
        this.commentId = commentId;
        this.createdAt = createdAt;
        this.username = username;
        this.userProfileImage = userProfileImage;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }
}
