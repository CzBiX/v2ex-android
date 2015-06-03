package com.czbix.v2ex.eventbus;

public class LoginEvent extends BusEvent {
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
