package com.czbix.v2ex.common;

import android.widget.Toast;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.eventbus.BaseEvent.DailyAwardEvent;
import com.czbix.v2ex.eventbus.BaseEvent.NewUnreadEvent;
import com.czbix.v2ex.eventbus.LoginEvent;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.parser.MyselfParser;
import com.czbix.v2ex.util.CrashlyticsUtils;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.UserUtils;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class UserState {
    private static final UserState instance;

    private String mUsername;
    private boolean mHasUnread;
    private boolean mHasAward;

    static {
        instance = new UserState();
    }

    public static UserState getInstance() {
        return instance;
    }

    public void init() {
        AppCtx.getEventBus().register(this);

        mUsername = ConfigDao.get(ConfigDao.KEY_USERNAME, null);
        CrashlyticsUtils.setUserState(isLoggedIn());
    }

    public void handleInfo(MyselfParser.MySelfInfo info, boolean isTab) {
        if (info == null) {
            logout();
            return;
        }

        final EventBus eventBus = AppCtx.getEventBus();
        if (info.mUnread > 0) {
            mHasUnread = true;
            eventBus.post(new NewUnreadEvent(info.mUnread));
        }
        if (isTab && info.mHasAward != mHasAward) {
            eventBus.post(new DailyAwardEvent(info.mHasAward));
        }
    }

    public void login(String username, Avatar avatar) {
        ConfigDao.put(ConfigDao.KEY_AVATAR, avatar.getBaseUrl());
        ConfigDao.put(ConfigDao.KEY_USERNAME, username);

        mUsername = username;
        CrashlyticsUtils.setUserState(true);

        AppCtx.getEventBus().post(new LoginEvent(username));
        ExecutorUtils.execute(new Runnable() {
            @Override
            public void run() {
                UserUtils.checkDailyAward();
            }
        });
    }

    public void logout() {
        mUsername = null;
        RequestHelper.clearCookies();

        ConfigDao.remove(ConfigDao.KEY_USERNAME);
        ConfigDao.remove(ConfigDao.KEY_AVATAR);

        CrashlyticsUtils.setUserState(false);

        ExecutorUtils.runInUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppCtx.getInstance(), R.string.toast_has_sign_out,
                        Toast.LENGTH_LONG).show();
            }
        });
        AppCtx.getEventBus().post(new LoginEvent());
    }

    @Subscribe
    public void onDailyMissionEvent(DailyAwardEvent e) {
        mHasAward = e.mHasAward;
    }

    public boolean isLoggedIn() {
        return mUsername != null;
    }

    public String getUsername() {
        return mUsername;
    }

    public boolean hasUnread() {
        return mHasUnread;
    }

    public void clearUnread() {
        mHasUnread = false;
        AppCtx.getEventBus().post(new NewUnreadEvent(0));
    }

    public boolean hasAward() {
        return mHasAward;
    }
}
