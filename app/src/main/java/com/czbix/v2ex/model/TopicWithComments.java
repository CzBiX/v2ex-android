package com.czbix.v2ex.model;

import androidx.annotation.NonNull;

import java.util.List;

public class TopicWithComments {
    public final Topic mTopic;
    public final List<Comment> mComments;
    public final int mCurPage;
    public final int mMaxPage;
    public final String mCsrfToken;
    public final String mOnceToken;
    public int mLastReadPos;

    public TopicWithComments(@NonNull Topic topic, @NonNull List<Comment> comments,
                             int curPage, int maxPage,
                             String csrfToken, String onceToken) {
        mTopic = topic;
        mComments = comments;
        mCurPage = curPage;
        mMaxPage = maxPage;
        mCsrfToken = csrfToken;
        mOnceToken = onceToken;
    }
}
