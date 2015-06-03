package com.czbix.v2ex.eventbus;

public class CommentEvent extends BusEvent {
    public final boolean mIsReply;

    /**
     * reply event
     */
    public CommentEvent() {
        this(false);
    }

    public CommentEvent(boolean isReply) {
        mIsReply = isReply;
    }
}
