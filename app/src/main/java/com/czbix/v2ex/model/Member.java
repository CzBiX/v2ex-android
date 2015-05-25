package com.czbix.v2ex.model;

public class Member {
    private int mId;
    private String mUsername;
    private String mTagLine;
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

    public String getTagLine() {
        return mTagLine;
    }

    public String getUsername() {
        return mUsername;
    }
}
