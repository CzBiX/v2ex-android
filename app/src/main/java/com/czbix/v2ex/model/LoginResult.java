package com.czbix.v2ex.model;

public class LoginResult {
    public final Avatar mAvatar;
    public final String mUsername;

    public LoginResult(String username, Avatar avatar) {
        mAvatar = avatar;
        mUsername = username;
    }
}
