package com.czbix.v2ex.model;

import android.support.annotation.NonNull;

import java.util.List;

public class TopicWithComments {
    public final Topic mTopic;
    public final List<Comment> mComments;
    public final String mCsrfToken;
    public final String mOnceToken;

    public TopicWithComments(@NonNull Topic topic, @NonNull List<Comment> comments,
                             String csrfToken, String onceToken) {
        mTopic = topic;
        mComments = comments;
        mCsrfToken = csrfToken;
        mOnceToken = onceToken;
    }
}
