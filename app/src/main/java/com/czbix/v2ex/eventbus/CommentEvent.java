package com.czbix.v2ex.eventbus;

import android.support.annotation.IntDef;

public class CommentEvent extends BusEvent {
    @IntDef({TYPE_REPLY, TYPE_THANK, TYPE_IGNORE_TOPIC, TYPE_IGNORE_COMMENT})
    @interface CommentType {}
    public static final int TYPE_REPLY = 1;
    public static final int TYPE_THANK = 2;
    public static final int TYPE_IGNORE_TOPIC = 3;
    public static final int TYPE_IGNORE_COMMENT = 4;

    @CommentType
    public final int mType;

    public CommentEvent(@CommentType int type) {
        mType = type;
    }
}
