package com.cookbook.fenix.cookbook;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by fenix on 01.07.2015.
 */
public class Recipe implements Serializable {
    private String title;
    private String recipeID;
    private String socialRank;
    private String publisher;
    private String[] ingredients;
    private String imgURL;

    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    private Bitmap bmp;

    public Recipe() {
    }

    public Recipe(String title, String recipeID, String socialRank, String publisher, String imgURL) {

        this.title = title;
        this.recipeID = recipeID;
        this.socialRank = socialRank;
        this.publisher = publisher;
        this.ingredients = ingredients;
        this.imgURL = imgURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    public String getSocialRank() {
        return socialRank;
    }

    public void setSocialRank(String socialRank) {
        this.socialRank = socialRank;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String[] getIngredients() {
        return ingredients;
    }

    public void setIngredients(String[] ingredients) {
        this.ingredients = ingredients;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }
}
