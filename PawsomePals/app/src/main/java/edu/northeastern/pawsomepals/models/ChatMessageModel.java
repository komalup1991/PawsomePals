package edu.northeastern.pawsomepals.models;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
    private String senderName;
    private String message;
    private String senderId;
    private Timestamp timestamp;
    private boolean picture;
    private boolean place;
    private String image;
    private ChatLocationModel location;

    public ChatMessageModel() {
    }

    public ChatMessageModel(String message, String senderId, Timestamp Timestamp, String senderName, String image, ChatLocationModel location) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = Timestamp;
        this.senderName = senderName;
        this.image = image;
        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp Timestamp) {
        this.timestamp = Timestamp;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public boolean isPicture() {
        return picture;
    }

    public void setPlace(Boolean isPlace) {
        this.place = isPlace;
    }

    public boolean isPlace() {
        return place;
    }

    public void setPicture(boolean picture) {
        this.picture = picture;
    }

    public String getImage() {
        return image;
    }

    public ChatLocationModel getLocation() {
        return location;
    }

    public void setLocation(ChatLocationModel location) {
        this.location = location;
    }
}
