package com.czbix.v2ex.model;

import android.os.Parcel;

import java.util.regex.Pattern;

public class Node extends Page {
    private static final Pattern PATTERN = Pattern.compile("/go/(.+?)(?:\\W|$)");

    private int mId;
    private String mName;
    private String mAlternative;
    private int mTopics;
    private Avatar mAvatar;

    public Node(String title, int id, Avatar avatar, String name, String alternative,
                int topics) {
        super(title);

        mId = id;
        mAvatar = avatar;
        mName = name;
        mAlternative = alternative;
        mTopics = topics;
    }

    public int getId() {
        return mId;
    }

    public Avatar getAvatar() {
        return mAvatar;
    }

    public String getName() {
        return mName;
    }

    public String getAlternative() {
        return mAlternative;
    }

    public int getTopics() {
        return mTopics;
    }

    public static String buildUrlByName(String name) {
        return "/go/" + name;
    }

    public static String getNameFromUrl(String url) {
        return PATTERN.matcher(url).group(1);
    }

    @Override
    public String getUrl() {
        return buildUrlByName(getName());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getTitle());
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mAlternative);
        dest.writeInt(mTopics);
        mAvatar.writeToParcel(dest, flags);
    }

    public static final Creator<Node> CREATOR = new Creator<Node>() {
        @Override
        public Node createFromParcel(Parcel source) {
            return new Builder()
                    .setTitle(source.readString())
                    .setId(source.readInt())
                    .setName(source.readString())
                    .setTitleAlternative(source.readString())
                    .setTopics(source.readInt())
                    .setAvatar(Avatar.CREATOR.createFromParcel(source))
                    .createNode();
        }

        @Override
        public Node[] newArray(int size) {
            return new Node[size];
        }
    };

    public static class Builder {
        private int mId;
        private String mTitle;
        private Avatar mAvatar;
        private String mName;
        private String mTitleAlternative;
        private int mTopics;

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setAvatar(Avatar avatar) {
            mAvatar = avatar;
            return this;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setTitleAlternative(String titleAlternative) {
            mTitleAlternative = titleAlternative;
            return this;
        }

        public Builder setTopics(int topics) {
            mTopics = topics;
            return this;
        }

        public Node createNode() {
            return new Node(mTitle, mId, mAvatar, mName, mTitleAlternative, mTopics);
        }
    }
}
