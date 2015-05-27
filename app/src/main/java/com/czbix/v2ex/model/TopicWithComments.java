package com.czbix.v2ex.model;

import java.util.List;

public class TopicWithComments {
    public final Topic mTopic;
    public final List<String> mComments;

    public TopicWithComments(Topic topic, List<String> comments) {
        mComments = comments;
        mTopic = topic;
    }
}
