package com.hst.beautifulwall.models.favorite;

public class FavoriteModel {
    int id;
    String title;
    String imageUrl;
    String postCategory;

    public FavoriteModel(int id, String title, String imageUrl, String postCategory) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.postCategory = postCategory;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPostCategory() {
        return postCategory;
    }
}