package com.czbix.v2ex.eventbus;

import android.support.annotation.IntDef;

public class TopicEvent extends BaseEvent {
    @EventType
    public final int mType;

    public TopicEvent(@EventType int type) {
        mType = type;
    }

    @IntDef({TYPE_REPLY, TYPE_THANK, TYPE_IGNORE_TOPIC, TYPE_IGNORE_COMMENT, TYPE_FAV_TOPIC})
    @interface EventType {}
    public static final int TYPE_REPLY = 1;
    public static final int TYPE_THANK = 2;
    public static final int TYPE_IGNORE_TOPIC = 3;
    public static final int TYPE_IGNORE_COMMENT = 4;
    public static final int TYPE_FAV_TOPIC = 5;
}
