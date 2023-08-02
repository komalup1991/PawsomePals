package edu.northeastern.pawsomepals.models;

import androidx.annotation.Nullable;

public class Event extends FeedItem {
    private String eventName;
    private String img;
    private String eventDate;
    private String eventTime;
    private String eventDetails;

    public Event() {
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
    @Override
    public int hashCode() {
        return getFeedItemId().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Event otherEvent)) {
            return false;
        }

        if (otherEvent.getFeedItemId() == null || this.getFeedItemId() == null) {
            return false;
        }

        return this.getFeedItemId().equals(otherEvent.getFeedItemId());
    }
}
