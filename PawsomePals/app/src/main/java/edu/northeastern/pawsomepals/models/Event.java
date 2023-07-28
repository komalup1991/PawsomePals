package edu.northeastern.pawsomepals.models;

public class Event extends FeedItem {
    private String eventName;
    private String img;
    private String eventDate;
    private String eventTime;
    private String eventDetails;
    private String eventId;

    public Event() {
    }

    public Event(String username, String userProfileImage, String createdAt, String userTagged, String locationTagged, String createdBy, String eventName, String img, String eventDate, String eventTime, String eventDetails) {
        super(username, userProfileImage, createdAt, userTagged, locationTagged, createdBy);
        this.eventName = eventName;
        this.img = img;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventDetails = eventDetails;
    }

    @Override
    public int getType() {
        return FeedItem.TYPE_EVENT;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public String getEventTime() {
        return eventTime;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getImg() {
        return img;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }


}
