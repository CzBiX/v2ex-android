package com.czbix.v2ex.common;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.eventbus.LoginEvent;
import com.google.common.eventbus.Subscribe;

public class UserState {
    private static final UserState instance;

    private String mUsername;

    static {
        instance = new UserState();
    }

    public static UserState getInstance() {
        return instance;
    }

    public UserState() {
        mUsername = ConfigDao.get(ConfigDao.KEY_USERNAME, null);

        AppCtx.getEventBus().register(this);
    }

    @Subscribe
    public void onLoginEvent(LoginEvent e) {
        mUsername = e.mUsername;
    }

    public boolean isAnonymous() {
        return mUsername == null;
    }

    public String getUsername() {
        return mUsername;
    }
}
