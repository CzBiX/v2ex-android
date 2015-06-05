package com.czbix.v2ex.common;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.eventbus.BusEvent.NewUnreadEvent;
import com.czbix.v2ex.eventbus.LoginEvent;
import com.czbix.v2ex.parser.MyselfParser;
import com.czbix.v2ex.util.UserUtils;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;

public class UserState {
    private static final UserState instance;

    private String mUsername;
    private int mLastReadCount;
    private boolean mHasUnread;

    static {
        instance = new UserState();
    }

    public static UserState getInstance() {
        return instance;
    }

    public void init() {
        mUsername = ConfigDao.get(ConfigDao.KEY_USERNAME, null);
        if (!Strings.isNullOrEmpty(mUsername)) {
            mLastReadCount = ConfigDao.get(ConfigDao.KEY_NOTIFICATION_COUNT, 0);
        }

        AppCtx.getEventBus().register(this);
    }

    public void handleInfo(MyselfParser.MySelfInfo info) {
        if (info == null) {
            UserUtils.logout();
            return;
        }

        if (info.mUnread > 0) {
            mHasUnread = true;
            AppCtx.getEventBus().post(new NewUnreadEvent());
        }
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

    public boolean hasUnread() {
        return mHasUnread;
    }
}
