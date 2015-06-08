package com.czbix.v2ex.model;

import android.os.Parcel;

import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.network.RequestHelper;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Topic extends Page implements ThankAble, IgnoreAble {
    private static final Pattern PATTERN = Pattern.compile("/t/(\\d+?)(?:\\W|$)");

    private final int mId;
    private final String mTitle;
    private final String mContent;
    private final int mReplies;
    private final Member mMember;
    private final Node mNode;
    private final String mReplyTime;
    private final boolean mHasInfo;
    private boolean mHasRead;

    Topic(String title, int id, String content, Member member, Node node, String replyTime, int replies) {
        Preconditions.checkArgument(id != 0);

        mId = id;

        mTitle = title;
        mContent = content;
        mMember = member;
        mNode = node;
        mReplies = replies;
        mReplyTime = replyTime;

        mHasInfo = member != null;
    }

    public boolean hasInfo() {
        return mHasInfo;
    }

    public int getId() {
        return mId;
    }

    public String getReplyTime() {
        return mReplyTime;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public Member getMember() {
        return mMember;
    }

    public Node getNode() {
        return mNode;
    }

    public int getReplyCount() {
        return mReplies;
    }

    public static String buildUrlFromId(int id) {
        return RequestHelper.BASE_URL + "/t/" + Integer.toString(id);
    }

    public static int getIdFromUrl(String url) {
        final Matcher matcher = PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new FatalException("match id for topic failed: " + url);
        }
        final String idStr = matcher.group(1);
        return Integer.parseInt(idStr);
    }

    public String getContent() {
        return mContent;
    }

    @Override
    public String getUrl() {
        return buildUrlFromId(mId);
    }

    @Override
    public String getIgnoreUrl() {
        return String.format("%s/ignore/topic/%d", RequestHelper.BASE_URL, mId);
    }

    @Override
    public String getThankUrl() {
        return String.format("%s/thank/topic/%d", RequestHelper.BASE_URL, mId);
    }

    public boolean hasRead() {
        return mHasRead;
    }

    public void setHasRead() {
        mHasRead = true;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeByte(mHasInfo ? (byte) 1 : (byte) 0);
        dest.writeString(this.mTitle);
        dest.writeString(this.mContent);
        dest.writeInt(this.mReplies);
        dest.writeParcelable(this.mMember, 0);
        dest.writeParcelable(this.mNode, 0);
        dest.writeString(this.mReplyTime);
    }

    protected Topic(Parcel in) {
        this.mId = in.readInt();
        this.mHasInfo = in.readByte() != 0;
        this.mTitle = in.readString();
        this.mContent = in.readString();
        this.mReplies = in.readInt();
        this.mMember = in.readParcelable(Member.class.getClassLoader());
        this.mNode = in.readParcelable(Node.class.getClassLoader());
        this.mReplyTime = in.readString();
    }

    public static final Creator<Topic> CREATOR = new Creator<Topic>() {
        public Topic createFromParcel(Parcel source) {
            return new Topic(source);
        }

        public Topic[] newArray(int size) {
            return new Topic[size];
        }
    };

    public Builder toBuilder() {
        return new Builder()
                .setId(mId)
                .setTitle(mTitle)
                .setContent(mContent)
                .setMember(mMember)
                .setNode(mNode)
                .setReplyCount(mReplies)
                .setReplyTime(mReplyTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic)) return false;
        Topic topic = (Topic) o;
        return Objects.equal(mId, topic.mId) &&
                Objects.equal(mReplies, topic.mReplies) &&
                Objects.equal(mContent, topic.mContent) &&
                Objects.equal(mNode, topic.mNode) &&
                Objects.equal(mReplyTime, topic.mReplyTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mContent, mReplies, mNode, mReplyTime);
    }

    public static class Builder {
        private int mId;
        private String mTitle;
        private String mContent;
        private Member mMember;
        private Node mNode;
        private int mReplies;
        private String mReplyTime;

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

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

        public Builder setNode(Node node) {
            mNode = node;
            return this;
        }

        public Builder setReplyCount(int replies) {
            mReplies = replies;
            return this;
        }

        public Builder setReplyTime(String replyTime) {
            mReplyTime = replyTime;
            return this;
        }

        public boolean hasInfo() {
            return mMember != null;
        }

        public Topic createTopic() {
            return new Topic(mTitle, mId, mContent, mMember, mNode, mReplyTime, mReplies);
        }
    }
}
