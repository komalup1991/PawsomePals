package edu.northeastern.pawsomepals.models;

import androidx.annotation.Nullable;

public class Services extends FeedItem {
    private String serviceType;
    private String serviceName;
    private String serviceNotes;

    public Services() {

    }

    @Override
    public int getType() {
        return FeedItem.TYPE_SERVICE;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceNotes() {
        return serviceNotes;
    }
    @Override
    public int hashCode() {
        return getFeedItemId().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Services otherServices)) {
            return false;
        }

        if (otherServices.getFeedItemId() == null || this.getFeedItemId() == null) {
            return false;
        }

        return this.getFeedItemId().equals(otherServices.getFeedItemId());
    }
}
