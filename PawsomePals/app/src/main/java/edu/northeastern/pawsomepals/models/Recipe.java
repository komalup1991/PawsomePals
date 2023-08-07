package edu.northeastern.pawsomepals.models;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Recipe extends FeedItem implements Serializable {
    private String title;
    private String img;
    private String desc;
    private String createdBy;
    private String ingredients;
    private String serving;
    private String prepTime;
    private String cookTime;
    private String username;
    private String userProfileImage;
    private String createdAt;



    private String instructions;

    public Recipe() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public int getType() {
        return FeedItem.TYPE_RECIPE;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getServing() {
        return serving;
    }

    public void setServing(String serving) {
        this.serving = serving;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getCookTime() {
        return cookTime;
    }

    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    @Override
    public int hashCode() {
        return getFeedItemId().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Recipe otherRecipe)) {
            return false;
        }

        if (otherRecipe.getFeedItemId() == null || this.getFeedItemId() == null) {
            return false;
        }

        return this.getFeedItemId().equals(otherRecipe.getFeedItemId());
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "title='" + title + '\'' +
                ", img='" + img + '\'' +
                ", desc='" + desc + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", ingredients='" + ingredients + '\'' +
                ", serving='" + serving + '\'' +
                ", prepTime='" + prepTime + '\'' +
                ", cookTime='" + cookTime + '\'' +
                ", username='" + username + '\'' +
                ", userProfileImage='" + userProfileImage + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
