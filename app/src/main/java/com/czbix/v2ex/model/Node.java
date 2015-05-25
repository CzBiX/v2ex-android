package com.czbix.v2ex.model;

public class Node {
    private int mId;
    private String mName;
    private String mTitle;
    private String mTitleAlternative;
    private String mUrl;
    private int mTopics;
    private String mAvatarMini;
    private String mAvatarNormal;
    private String mAvatarLarge;

    public String getAvatarLarge() {
        return mAvatarLarge;
    }

    public String getAvatarMini() {
        return mAvatarMini;
    }

    public String getAvatarNormal() {
        return mAvatarNormal;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getTitleAlternative() {
        return mTitleAlternative;
    }

    public int getTopics() {
        return mTopics;
    }

    public String getUrl() {
        return mUrl;
    }
}
