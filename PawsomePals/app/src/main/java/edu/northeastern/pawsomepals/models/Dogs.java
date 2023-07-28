package edu.northeastern.pawsomepals.models;

public class Dogs {
    private String userId;
    private String name;
    private String breed;
    private Boolean isMixedBreed;
    private String mixedBreed;
    private String profileImage;
    private String gender;
    private String dob;
    private String size;

    public Dogs() {
    }

    public Dogs(String name, String breed, Boolean isMixedBreed, String mixedBreed, String profileImage, String gender, String dob, String size) {
        this.userId = userId;
        this.name = name;
        this.breed = breed;
        this.isMixedBreed = isMixedBreed;
        this.mixedBreed = mixedBreed;
        this.profileImage = profileImage;
        this.gender = gender;
        this.dob = dob;
        this.size = size;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Boolean getMixedBreed() {
        return isMixedBreed;
    }

    public void setMixedBreed(String mixedBreed) {
        this.mixedBreed = mixedBreed;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setMixedBreed(Boolean mixedBreed) {
        isMixedBreed = mixedBreed;
    }
}
