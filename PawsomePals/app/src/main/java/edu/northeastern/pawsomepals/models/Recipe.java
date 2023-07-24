package edu.northeastern.pawsomepals.models;

public class Recipe {
    private String recipeId;
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

    public Recipe() {
    }

    public Recipe(String recipeId, String title, String img, String desc, String createdBy, String ingredients, String serving, String prepTime, String cookTime) {
        this.recipeId = recipeId;
        this.title = title;
        this.img = img;
        this.desc = desc;
        this.createdBy = createdBy;
        this.ingredients = ingredients;
        this.serving = serving;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
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
