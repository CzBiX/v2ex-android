package com.czbix.v2ex.eventbus;

public abstract class BaseEvent {
    public static class GetNodesFinishEvent extends BaseEvent {}
    public static class NewUnreadEvent extends BaseEvent {
        public final int mCount;

        public NewUnreadEvent(int count) {
            mCount = count;
        }

        public boolean hasNew() {
            return mCount > 0;
        }
    }

    public static class DailyAwardEvent extends BaseEvent {
        public final boolean mHasAward;

        public DailyAwardEvent(boolean hasAward) {
            mHasAward = hasAward;
        }
    }

    public static class ContextInitFinishEvent extends BaseEvent {}
}
