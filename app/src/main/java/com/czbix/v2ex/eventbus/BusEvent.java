package com.czbix.v2ex.eventbus;

public abstract class BusEvent {
    public static class GetNodesFinishEvent extends BusEvent {}

    public static class LoginEvent extends BusEvent {
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
}
