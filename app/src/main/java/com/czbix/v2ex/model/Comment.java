package com.czbix.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable {
    private final String mContent;

    Comment(String content) {
        mContent = content;
    }

    public String getContent() {
        return mContent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mContent);
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source.readString());
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public static class Builder {
        private String mContent;

        public Builder setContent(String content) {
            mContent = content;
            return this;
        }

        public Comment createComment() {
            return new Comment(mContent);
        }
    }
}
