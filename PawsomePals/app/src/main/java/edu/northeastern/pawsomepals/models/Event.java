package edu.northeastern.pawsomepals.models;

public class Event extends FeedItemWithImage {
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String eventDetails;

    public Event() {
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public void setEventDetails(String eventDetails) {
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

    public String getEventName() {
        return eventName;
    }

}
