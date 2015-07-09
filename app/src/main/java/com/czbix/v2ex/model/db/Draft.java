package com.czbix.v2ex.model.db;

import java.util.concurrent.TimeUnit;

public class Draft {
    public final long mId;
    public final int mTopicId;
    public final String mContent;
    public final long mTime;

    public Draft(long id, int topicId, String content, long time) {
        mId = id;
        mTopicId = topicId;
        mContent = content;
        mTime = time;
    }

    public boolean isExpired() {
        return isExpired(mTime);
    }

    public static boolean isExpired(long draftTime) {
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - draftTime) > 1;
    }
}
