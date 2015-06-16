package com.czbix.v2ex.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public class TopicWithComments {
    public final Topic mTopic;
    public final List<Comment> mComments;
    @Nullable
    public final List<Postscript> mPostscripts;
    public final int mMaxPage;
    public final String mCsrfToken;
    public final String mOnceToken;

    public TopicWithComments(@NonNull Topic topic, @NonNull List<Comment> comments,
                             @Nullable List<Postscript> postscripts, int maxPage,
                             String csrfToken, String onceToken) {
        mTopic = topic;
        mComments = comments;
        mPostscripts = postscripts;
        mMaxPage = maxPage;
        mCsrfToken = csrfToken;
        mOnceToken = onceToken;
    }
}
