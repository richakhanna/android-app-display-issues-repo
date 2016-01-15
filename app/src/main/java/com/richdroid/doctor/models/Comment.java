package com.richdroid.doctor.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by richa.khanna on 11/29/15.
 */
public class Comment implements Parcelable {

    private String user;
    private String body;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        public Comment createFromParcel(Parcel source) {
            Comment comment = new Comment();
            comment.user = source.readString();
            comment.body = source.readString();

            return comment;
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user);
        dest.writeString(body);

    }
}
