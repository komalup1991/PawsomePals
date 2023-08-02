package edu.northeastern.pawsomepals.models;

public class BreedHeightDetails {
    private String imperial;
    private String metric;

    public BreedHeightDetails(String imperial, String metric) {
        this.imperial = imperial;
        this.metric = metric;
    }

    public String getImperial() {
        return imperial;
    }

    public void setImperial(String imperial) {
        this.imperial = imperial;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }
}
