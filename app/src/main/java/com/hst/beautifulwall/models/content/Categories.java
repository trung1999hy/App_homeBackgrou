package com.hst.beautifulwall.models.content;

import android.os.Parcel;
import android.os.Parcelable;

public class Categories implements Parcelable {
    String title;
    String imageUrl;

    public Categories() {
    }

    public Categories(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(imageUrl);
    }

    protected Categories(Parcel in) {
        title = in.readString();
        imageUrl = in.readString();
    }

    public static Creator<Categories> getCREATOR() {
        return CREATOR;
    }

    public static final Creator<Categories> CREATOR = new Creator<Categories>() {
        @Override
        public Categories createFromParcel(Parcel source) {
            return new Categories(source);
        }

        @Override
        public Categories[] newArray(int size) {
            return new Categories[size];
        }
    };
}