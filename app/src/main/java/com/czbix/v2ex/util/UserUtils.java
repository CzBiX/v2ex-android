package com.czbix.v2ex.util;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.model.Avatar;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class UserUtils {
    public static void login(String username, Avatar avatar) {
        ConfigDao.put(ConfigDao.KEY_AVATAR, avatar.getBaseUrl());
        ConfigDao.put(ConfigDao.KEY_USERNAME, username);
    }

    public static Avatar getAvatar() {
        Preconditions.checkState(!Strings.isNullOrEmpty(AppCtx.getInstance().getUsername()));

        final String url = ConfigDao.get(ConfigDao.KEY_AVATAR, null);
        Preconditions.checkNotNull(url);

        return new Avatar.Builder().setBaseUrl(url).createAvatar();
    }
}
