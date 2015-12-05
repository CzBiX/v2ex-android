package com.czbix.v2ex.model;

import android.os.Parcel;

import com.czbix.v2ex.common.exception.FatalException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Member extends Page {
    private static final Pattern PATTERN = Pattern.compile("/member/(.+?)(?:\\W|$)");

    private String mUsername;
    private String mTagLine;
    private Avatar mAvatar;

    public Member(String username, Avatar avatar, String tagLine) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username));

        mUsername = username;
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
        return mUsername;
    }

    @Override
    public String getTitle() {
        return getUsername();
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

    public static class Builder {
        private static final Cache<String, Member> CACHE;

        static {
            CACHE = CacheBuilder.newBuilder()
                    .initialCapacity(32)
                    .maximumSize(128)
                    .softValues()
                    .build();
        }

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
            try {
                return CACHE.get(mUsername, () -> new Member(mUsername, mAvatar, mTagLine));
            } catch (ExecutionException e) {
                throw new FatalException(e);
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUsername);
        dest.writeString(this.mTagLine);
        dest.writeParcelable(this.mAvatar, 0);
    }

    protected Member(Parcel in) {
        this.mUsername = in.readString();
        this.mTagLine = in.readString();
        this.mAvatar = in.readParcelable(Avatar.class.getClassLoader());
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        public Member createFromParcel(Parcel source) {
            return new Member(source);
        }

        public Member[] newArray(int size) {
            return new Member[size];
        }
    };
}
