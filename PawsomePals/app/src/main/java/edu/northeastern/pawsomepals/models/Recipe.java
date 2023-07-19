package edu.northeastern.pawsomepals.models;

public class Recipe {
    private String name;
    private String img;
    private String desc;
    private String ingredients;
    private String servingSize;
    private String prepTime;
    private String cookTime;

    public Recipe(String name, String img, String desc, String ingredients, String servingSize, String prepTime, String cookTime) {
        this.name = name;
        this.img = img;
        this.desc = desc;
        this.ingredients = ingredients;
        this.servingSize = servingSize;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
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
}
