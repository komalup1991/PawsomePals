package edu.northeastern.pawsomepals.models;

public class Users {

    private String userId;
    private String email;
    private String name;
    private String recipeId;

    public Users() {
    }
    public Users(String name,String userId, String email) {
        this.name = name;
        this.userId = userId;
        this.email = email;
    }
    public Users(String userId, String email) {
        this.userId = userId;
        this.email = email;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }
}
