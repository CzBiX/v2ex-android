package com.czbix.v2ex.model.db;

import com.czbix.v2ex.model.Topic;

public class ViewHistory {
    public final Topic mTopic;
    /** in milliseconds */
    public final long mTime;

    public ViewHistory(Topic topic, long time) {
        mTime = time;
        mTopic = topic;
    }
}
