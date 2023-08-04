package edu.northeastern.pawsomepals.models;

import android.graphics.Bitmap;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
    private String senderName;
    private String message;
    private String senderId;
    private Timestamp timestamp;
    private boolean picture;

    public ChatMessageModel(){
    }

    public ChatMessageModel(String message, String senderId, Timestamp Timestamp,String senderName){
        this.message = message;
        this.senderId = senderId;
        this.timestamp = Timestamp;
        this.senderName = senderName;
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

    public void setPicture(boolean picture) {
        this.picture = picture;
    }

    public void setPictureLink(String url) {
    }
}
