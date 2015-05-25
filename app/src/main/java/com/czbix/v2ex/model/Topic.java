package com.czbix.v2ex.model;

public class Topic {
    private int mId;
    private String mTitle;
    private String mUrl;
    private String mContent;
    private String mContentRendered;
    private int mReplies;
    private Member mMember;
    private Node mNode;
    private long mCreated;
    private long mLastModified;
    private long mLastTouched;

    public String getContent() {
        return mContent;
    }

    public String getSummary(int maxLength) {
        maxLength -= 3;
        if (mContent.length() <= maxLength) {
            return mContent;
        }

        return mContent.substring(0, maxLength) + "...";
    }

    public String getContentRendered() {
        return mContentRendered;
    }

    public long getCreated() {
        return mCreated;
    }

    public int getId() {
        return mId;
    }

    public long getLastModified() {
        return mLastModified;
    }

    public long getLastTouched() {
        return mLastTouched;
    }

    public Member getMember() {
        return mMember;
    }

    public Node getNode() {
        return mNode;
    }

    public int getReplies() {
        return mReplies;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }
}
