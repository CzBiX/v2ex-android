package com.czbix.v2ex.model;

import android.os.Parcel;
import android.support.annotation.Nullable;

import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.dao.NodeDao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Node extends Page {
    private static final Pattern PATTERN = Pattern.compile("/go/(.+?)(?:\\W|$)");

    private int mId;
    private String mName;
    private String mTitleAlternative;
    private int mTopics;
    private Avatar mAvatar;

    public Node(String title, int id, Avatar avatar, String name, String alternative,
                int topics) {
        super(title);

        mId = id;
        mAvatar = avatar;
        mName = name;
        mTitleAlternative = alternative;
        mTopics = topics;
    }

    public int getId() {
        return mId;
    }

    @Nullable
    public Avatar getAvatar() {
        return mAvatar;
    }

    public String getName() {
        return mName;
    }

    public String getTitleAlternative() {
        return mTitleAlternative;
    }

    public int getTopics() {
        return mTopics;
    }

    public static String buildUrlByName(String name) {
        return "/go/" + name;
    }

    public static String getNameFromUrl(String url) {
        final Matcher matcher = PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new FatalException("match name for node failed: " + url);
        }
        return matcher.group(1);
    }

    @Override
    public String getUrl() {
        return buildUrlByName(getName());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
    }

    public static final Creator<Node> CREATOR = new Creator<Node>() {
        @Override
        public Node createFromParcel(Parcel source) {
            return NodeDao.get(source.readString());
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
