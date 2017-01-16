package com.czbix.v2ex.util;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.common.exception.RequestException;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.event.BaseEvent;
import com.czbix.v2ex.helper.RxBus;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.network.RequestHelper;
import com.google.common.base.Preconditions;

public class UserUtils {
    private static final String TAG = UserUtils.class.getSimpleName();

    public static Avatar getAvatar() {
        Preconditions.checkState(UserState.INSTANCE.isLoggedIn());

        final String url = ConfigDao.get(ConfigDao.KEY_AVATAR, null);
        Preconditions.checkNotNull(url);

        return new Avatar.Builder().setBaseUrl(url).createAvatar();
    }

    public static void checkDailyAward() {
        if (!UserState.INSTANCE.isLoggedIn()) {
            return;
        }

        boolean hasAward;
        try {
            hasAward = RequestHelper.INSTANCE.hasDailyAward();
        } catch (ConnectionException | RemoteException | RequestException e) {
            LogUtils.v(TAG, "check daily award failed", e);
            return;
        }

        if (hasAward) {
            RxBus.INSTANCE.post(new BaseEvent.DailyAwardEvent(true));
        }
    }
}
