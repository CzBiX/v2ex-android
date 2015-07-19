package com.czbix.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.czbix.v2ex.network.RequestHelper;
import com.google.common.base.Objects;

public class Comment implements Parcelable, Thankable, Ignorable {
    private final int mId;
    private final String mContent;
    private final Member mMember;
    private final String mReplyTime;
    private final int mThanks;
    private final int mFloor;
    private final boolean mThanked;

    Comment(int id, String content, Member member, String replyTime, int thanks, int floor, boolean thanked) {
        mId = id;
        mContent = content;
        mMember = member;
        mReplyTime = replyTime;
        mThanks = thanks;
        mFloor = floor;
        mThanked = thanked;
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

    public boolean isThanked() {
        return mThanked;
    }

    @Override
    public String getIgnoreUrl() {
        return String.format("%s/ignore/reply/%d", RequestHelper.BASE_URL, mId);
    }

    @Override
    public String getThankUrl() {
        return String.format("%s/thank/reply/%d", RequestHelper.BASE_URL, mId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return Objects.equal(mId, comment.mId) &&
                Objects.equal(mThanks, comment.mThanks) &&
                Objects.equal(mContent, comment.mContent) &&
                Objects.equal(mReplyTime, comment.mReplyTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mContent, mReplyTime, mThanks);
    }

    public static class Builder {
        private int mId;
        private String mContent;
        private Member mMember;
        private String mReplyTime;
        private int mThanks;
        private int mFloor;
        private boolean mThanked;

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

        public Builder setThanked(boolean thanked) {
            mThanked = thanked;
            return this;
        }

        public Comment createComment() {
            return new Comment(mId, mContent, mMember, mReplyTime, mThanks, mFloor, mThanked);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mContent);
        dest.writeParcelable(this.mMember, 0);
        dest.writeString(this.mReplyTime);
        dest.writeInt(this.mThanks);
        dest.writeInt(this.mFloor);
        dest.writeByte(mThanked ? (byte) 1 : (byte) 0);
    }

    protected Comment(Parcel in) {
        this.mId = in.readInt();
        this.mContent = in.readString();
        this.mMember = in.readParcelable(Member.class.getClassLoader());
        this.mReplyTime = in.readString();
        this.mThanks = in.readInt();
        this.mFloor = in.readInt();
        this.mThanked = in.readByte() != 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
