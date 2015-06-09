package com.czbix.v2ex.util;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.common.exception.RequestException;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.eventbus.BusEvent;
import com.czbix.v2ex.eventbus.LoginEvent;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.network.RequestHelper;
import com.google.common.base.Preconditions;

public class UserUtils {
    private static final String TAG = UserUtils.class.getSimpleName();

    public static void login(String username, Avatar avatar) {
        ConfigDao.put(ConfigDao.KEY_AVATAR, avatar.getBaseUrl());
        ConfigDao.put(ConfigDao.KEY_USERNAME, username);

        AppCtx.getEventBus().post(new LoginEvent(username));
    }

    public static Avatar getAvatar() {
        Preconditions.checkState(!UserState.getInstance().isGuest());

        final String url = ConfigDao.get(ConfigDao.KEY_AVATAR, null);
        Preconditions.checkNotNull(url);

        return new Avatar.Builder().setBaseUrl(url).createAvatar();
    }

    public static void logout() {
        RequestHelper.clearCookies();

        ConfigDao.remove(ConfigDao.KEY_USERNAME);
        ConfigDao.remove(ConfigDao.KEY_AVATAR);

        AppCtx.getEventBus().post(new LoginEvent());
    }

    public static void checkDailyAward() {
        if (UserState.getInstance().isGuest()) {
            return;
        }

        boolean hasAward;
        try {
            hasAward = RequestHelper.hasDailyAward();
        } catch (ConnectionException | RemoteException | RequestException e) {
            LogUtils.v(TAG, "check daily award failed", e);
            return;
        }

        if (hasAward) {
            AppCtx.getEventBus().post(new BusEvent.DailyAwardEvent(true));
        }
    }
}
