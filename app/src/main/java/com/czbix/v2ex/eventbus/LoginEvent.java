package com.czbix.v2ex.eventbus;

import com.czbix.v2ex.event.BaseEvent;

public class LoginEvent extends BaseEvent {
    public final String mUsername;

    public LoginEvent() {
        mUsername = null;
    }

    public LoginEvent(String username) {
        mUsername = username;
    }

    public boolean isLogOut() {
        return mUsername == null;
    }
}
