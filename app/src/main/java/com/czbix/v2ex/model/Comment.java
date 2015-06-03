package com.czbix.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.czbix.v2ex.network.RequestHelper;

public class Comment implements Parcelable {
    private final int mId;
    private final String mContent;
    private final Member mMember;
    private final String mReplyTime;
    private final int mThanks;
    private final int mFloor;

    Comment(int id, String content, Member member, String replyTime, int thanks, int floor) {
        mId = id;
        mContent = content;
        mMember = member;
        mReplyTime = replyTime;
        mThanks = thanks;
        mFloor = floor;
    }

    public int getId() {
        return mId;
    }

    public String getContent() {
        return mContent;
    }

    public Member getMember() {
        return mMember;
    }

    public String getReplyTime() {
        return mReplyTime;
    }

    public int getThanks() {
        return mThanks;
    }

    public int getFloor() {
        return mFloor;
    }

    public String getIgnoreUrl() {
        return String.format("%s/ignore/reply/%d", RequestHelper.BASE_URL, mId);
    }

    public String getThankUrl() {
        return String.format("%s/thank/reply/%d", RequestHelper.BASE_URL, mId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mContent);
        mMember.writeToParcel(dest, flags);
        dest.writeString(mReplyTime);
        dest.writeInt(mThanks);
        dest.writeInt(mFloor);
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment.Builder()
                    .setContent(source.readString())
                    .setMember(Member.CREATOR.createFromParcel(source))
                    .setReplyTime(source.readString())
                    .setThanks(source.readInt())
                    .setFloor(source.readInt())
                    .createComment();
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public static class Builder {
        private int mId;
        private String mContent;
        private Member mMember;
        private String mReplyTime;
        private int mThanks;
        private int mFloor;

        public Builder setId(int id) {
            mId = id;
            return this;
        }

        public Builder setContent(String content) {
            mContent = content;
            return this;
        }

        public Builder setMember(Member member) {
            mMember = member;
            return this;
        }

        public Builder setReplyTime(String time) {
            mReplyTime = time;
            return this;
        }

        public Builder setThanks(int thanks) {
            mThanks = thanks;
            return this;
        }

        public Builder setFloor(int floor) {
            mFloor = floor;
            return this;
        }

        public Comment createComment() {
            return new Comment(mId, mContent, mMember, mReplyTime, mThanks, mFloor);
        }
    }
}
