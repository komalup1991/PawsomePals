package edu.northeastern.pawsomepals.models;

public class Event {
    private String createdBy;
    private String eventName;
    private String img;
    private String eventDate;
    private String eventTime;
    private String eventDetails;
    private String userTagged;
    private String locationTagged;
    private String createdAt;

    public Event() {
    }

    public Event(String createdBy, String eventName, String img, String eventDate, String eventTime, String eventDetails, String userTagged, String locationTagged) {
        this.createdBy = createdBy;
        this.eventName = eventName;
        this.img = img;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventDetails = eventDetails;
        this.userTagged = userTagged;
        this.locationTagged = locationTagged;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public String getUserTagged() {
        return userTagged;
    }

    public void setUserTagged(String userTagged) {
        this.userTagged = userTagged;
    }

    public String getLocationTagged() {
        return locationTagged;
    }

    public void setLocationTagged(String locationTagged) {
        this.locationTagged = locationTagged;
    }
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Event{" +
                "createdBy='" + createdBy + '\'' +
                ", eventName='" + eventName + '\'' +
                ", img='" + img + '\'' +
                ", eventDate='" + eventDate + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", eventDetails='" + eventDetails + '\'' +
                ", userTagged='" + userTagged + '\'' +
                ", locationTagged='" + locationTagged + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
