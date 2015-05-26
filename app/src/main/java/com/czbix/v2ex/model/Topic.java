package com.czbix.v2ex.model;

import com.czbix.v2ex.common.exception.FatalException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Topic extends Page {
    private static final Pattern PATTERN = Pattern.compile("/t/(\\d+?)(?:\\W|$)");

    private int mId;
    private String mContent;
    private int mReplies;
    private Member mMember;
    private Node mNode;
    private String mReplyTime;

    Topic(String title, int id, String content, Member member, Node node, String replyTime, int replies) {
        super(title);

        mId = id;
        mContent = content;
        mMember = member;
        mNode = node;
        mReplies = replies;
        mReplyTime = replyTime;
    }

    public int getId() {
        return mId;
    }

    public String getReplyTime() {
        return mReplyTime;
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
        return "/t/" + Integer.toString(id);
    }

    public static int getIdFromUrl(String url) {
        final Matcher matcher = PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new FatalException("match id for topic failed: " + url);
        }
        final String idStr = matcher.group(1);
        return Integer.parseInt(idStr);
    }

    @Override
    public String getUrl() {
        return buildUrlFromId(mId);
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

        public Topic createTopic() {
            return new Topic(mTitle, mId, mContent, mMember, mNode, mReplyTime, mReplies);
        }
    }
}
