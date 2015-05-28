package com.czbix.v2ex.model;

import java.util.List;

public class TopicWithComments {
    public final Topic mTopic;
    public final List<Comment> mComments;

    public TopicWithComments(Topic topic, List<Comment> comments) {
        mTopic = topic;
        mComments = comments;
    }
}
