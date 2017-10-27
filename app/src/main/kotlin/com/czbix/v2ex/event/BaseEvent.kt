package com.czbix.v2ex.event

abstract class BaseEvent {
    class NewUnreadEvent(val mCount: Int) : BaseEvent() {
        fun hasNew(): Boolean {
            return mCount > 0
        }
    }

    class DailyAwardEvent(val mHasAward: Boolean) : BaseEvent()

    class ContextInitFinishEvent : BaseEvent()
}
