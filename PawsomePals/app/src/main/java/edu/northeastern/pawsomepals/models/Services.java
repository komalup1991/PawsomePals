package edu.northeastern.pawsomepals.models;

public class Services extends FeedItem {
    private String serviceType;
    private String serviceName;
    private String serviceNotes;
    private String serviceId;

    public Services() {

    }

    public Services(String username, String userProfileImage, String createdAt, String userTagged, String locationTagged, String createdBy, String serviceType, String serviceName, String serviceNotes,Long commentCount) {
        super(username, userProfileImage, createdAt, userTagged, locationTagged, createdBy,commentCount);
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

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

}
