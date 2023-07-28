package edu.northeastern.pawsomepals.models;

public class Services extends FeedItem {
    private String serviceType;
    private String serviceName;
    private String serviceNotes;

    public Services() {

    }

    public Services(String username, String userProfileImage, String createdAt, String userTagged, String locationTagged, String createdBy, String serviceType, String serviceName, String serviceNotes) {
        super(username, userProfileImage, createdAt, userTagged, locationTagged, createdBy);
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.serviceNotes = serviceNotes;
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
}
