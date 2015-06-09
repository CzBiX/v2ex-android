package com.czbix.v2ex.common;

import android.widget.Toast;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.eventbus.BusEvent.DailyAwardEvent;
import com.czbix.v2ex.eventbus.BusEvent.NewUnreadEvent;
import com.czbix.v2ex.eventbus.LoginEvent;
import com.czbix.v2ex.parser.MyselfParser;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.UserUtils;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class UserState {
    private static final UserState instance;

    private String mUsername;
    private int mLastReadCount;
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
        if (!Strings.isNullOrEmpty(mUsername)) {
            mLastReadCount = ConfigDao.get(ConfigDao.KEY_NOTIFICATION_COUNT, 0);
        }
    }

    public void handleInfo(MyselfParser.MySelfInfo info, boolean isTab) {
        if (info == null) {
            UserUtils.logout();
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

    @Subscribe
    public void onLoginEvent(LoginEvent e) {
        mUsername = e.mUsername;

        if (isGuest()) {
            Toast.makeText(AppCtx.getInstance(), R.string.toast_has_sign_out, Toast.LENGTH_LONG).show();
            return;
        }
        ExecutorUtils.execute(new Runnable() {
            @Override
            public void run() {
                UserUtils.checkDailyAward();
            }
        });
    }

    @Subscribe
    public void onDailyMissionEvent(DailyAwardEvent e) {
        mHasAward = e.mHasAward;
    }

    public boolean isGuest() {
        return mUsername == null;
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
