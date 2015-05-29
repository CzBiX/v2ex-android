package com.czbix.v2ex.eventbus;

public abstract class BusEvent {
    public static class GetNodesFinishEvent extends BusEvent {}
    public static class LoginSuccessEvent extends BusEvent {
        public final String mUsername;

        public LoginSuccessEvent(String username) {
            mUsername = username;
        }
    }
}
