package edu.northeastern.pawsomepals.models;

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

}
