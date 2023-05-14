package com.hst.beautifulwall.models.content;

import android.os.Parcel;
import android.os.Parcelable;

public class Posts implements Parcelable {
    String title;
    String category;
    String isFeatured;
    String imageUrl;
    boolean isFavorite;

    public Posts() {
    }

    public Posts(String title, String category, String isFeatured, String imageUrl, boolean isFavorite) {
        this.title = title;
        this.category = category;
        this.isFeatured = isFeatured;
        this.imageUrl = imageUrl;
        this.isFavorite = isFavorite;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getIsFeatured() {
        return isFeatured;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(category);
        dest.writeString(isFeatured);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }

    protected Posts(Parcel in) {
        title = in.readString();
        category = in.readString();
        isFeatured = in.readString();
        imageUrl = in.readString();
        isFavorite = in.readByte() != 0;
    }

    public static Creator<Posts> getCREATOR() {
        return CREATOR;
    }

    public static final Creator<Posts> CREATOR = new Creator<Posts>() {
        @Override
        public Posts createFromParcel(Parcel source) {
            return new Posts(source);
        }

        @Override
        public Posts[] newArray(int size) {
            return new Posts[size];
        }
    };
}