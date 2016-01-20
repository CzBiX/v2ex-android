package com.czbix.v2ex.model.json;

import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Topic;

public class TopicBean {
    public int mId;
    public String mTitle;
    public String mContent_rendered;
    public int mReplies;
    public MemberBean mMember;
    public Node mNode;

    public Topic toModel() {
        return new Topic.Builder()
                .setId(mId)
                .setTitle(mTitle)
                .setContent(mContent_rendered)
                .setReplyCount(mReplies)
                .setMember(mMember.toModel())
                .setNode(mNode).createTopic();
    }
}
