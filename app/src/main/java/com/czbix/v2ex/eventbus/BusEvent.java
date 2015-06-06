package com.czbix.v2ex.eventbus;

public abstract class BusEvent {
    public static class GetNodesFinishEvent extends BusEvent {}
    public static class NewUnreadEvent extends BusEvent {}

    public static class DailyAwardEvent extends BusEvent {
        public final boolean mHasAward;

        public DailyAwardEvent(boolean hasAward) {
            mHasAward = hasAward;
        }
    }
}
