package edu.northeastern.pawsomepals.models;

public class Services {
    private String createdBy;
    private String serviceType;
    private String serviceName;
    private String serviceNotes;
    private String userTagged;
    private String locationTagged;
    private String createdAt;

    public Services() {
    }

    public Services(String createdBy, String serviceType, String serviceName, String serviceNotes, String userTagged, String locationTagged) {
        this.createdBy = createdBy;
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.serviceNotes = serviceNotes;
        this.userTagged = userTagged;
        this.locationTagged = locationTagged;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceNotes() {
        return serviceNotes;
    }

    public void setServiceNotes(String serviceNotes) {
        this.serviceNotes = serviceNotes;
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
        return "Services{" +
                "createdBy='" + createdBy + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceNotes='" + serviceNotes + '\'' +
                ", userTagged='" + userTagged + '\'' +
                ", locationTagged='" + locationTagged + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
