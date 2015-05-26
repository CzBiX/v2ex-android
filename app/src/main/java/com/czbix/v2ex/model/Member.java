package com.czbix.v2ex.model;

import android.os.Parcel;

import com.czbix.v2ex.common.exception.FatalException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Member extends Page {
    private static final Pattern PATTERN = Pattern.compile("/member/(.+?)(?:\\W|$)");

    private String mTagLine;
    private Avatar mAvatar;

    public Member(String username, Avatar avatar, String tagLine) {
        super(username);
        mAvatar = avatar;
        mTagLine = tagLine;
    }

    public Avatar getAvatar() {
        return mAvatar;
    }

    public String getTagLine() {
        return mTagLine;
    }

    public String getUsername() {
        return getTitle();
    }

    public static String getNameFromUrl(String url) {
        final Matcher matcher = PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new FatalException("match name for member failed: " + url);
        }
        return matcher.group(1);
    }

    public static String buildUrlFromName(String username) {
        return "/member/" + username;
    }

    @Override
    public String getUrl() {
        return buildUrlFromName(getUsername());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getUsername());
        mAvatar.writeToParcel(dest, flags);
        dest.writeString(mTagLine);
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel source) {
            return new Builder()
                    .setUsername(source.readString())
                    .setAvatar(Avatar.CREATOR.createFromParcel(source))
                    .setTagLine(source.readString())
                    .createMember();
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    public static class Builder {
        private String mUsername;
        private Avatar mAvatar;
        private String mTagLine;

        public Builder setUsername(String title) {
            mUsername = title;
            return this;
        }

        public Builder setAvatar(Avatar avatar) {
            mAvatar = avatar;
            return this;
        }

        public Builder setTagLine(String tagLine) {
            mTagLine = tagLine;
            return this;
        }

        public Member createMember() {
            return new Member(mUsername, mAvatar, mTagLine);
        }
    }
}
