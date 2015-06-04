package com.czbix.v2ex.util;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.eventbus.LoginEvent;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.network.RequestHelper;
import com.google.common.base.Preconditions;

public class UserUtils {
    public static void login(String username, Avatar avatar) {
        ConfigDao.put(ConfigDao.KEY_AVATAR, avatar.getBaseUrl());
        ConfigDao.put(ConfigDao.KEY_USERNAME, username);
        AppCtx.getEventBus().post(new LoginEvent(username));
    }

    public static Avatar getAvatar() {
        Preconditions.checkState(!UserState.getInstance().isAnonymous());

        final String url = ConfigDao.get(ConfigDao.KEY_AVATAR, null);
        Preconditions.checkNotNull(url);

        return new Avatar.Builder().setBaseUrl(url).createAvatar();
    }

    public static void logout() {
        RequestHelper.clearCookies();
        ConfigDao.remove(ConfigDao.KEY_USERNAME);
        AppCtx.getEventBus().post(new LoginEvent());
    }
}
