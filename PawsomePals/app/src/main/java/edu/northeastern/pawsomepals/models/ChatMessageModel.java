package edu.northeastern.pawsomepals.models;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
    private String message;
    private String senderId;
    private Timestamp timestamp;

    public ChatMessageModel(){
    }

    public ChatMessageModel(String message, String senderId, Timestamp Timestamp){
        this.message = message;
        this.senderId = senderId;
        this.timestamp = Timestamp;
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
}
