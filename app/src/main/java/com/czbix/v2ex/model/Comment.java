package com.czbix.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.czbix.v2ex.network.RequestHelper;
import com.google.common.base.Objects;

public class Comment implements Parcelable {
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

    public String getIgnoreUrl() {
        return String.format("%s/ignore/reply/%d", RequestHelper.BASE_URL, mId);
    }

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
}
