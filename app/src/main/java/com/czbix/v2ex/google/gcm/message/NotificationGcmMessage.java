package com.czbix.v2ex.google.gcm.message;

import android.content.Context;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.eventbus.BaseEvent.NewUnreadEvent;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.util.LogUtils;

import java.util.Random;

public class NotificationGcmMessage extends GcmMessage {
    private static final String TAG = NotificationGcmMessage.class.getSimpleName();

    public static final String MSG_TYPE = "notifications";

    @Override
    protected void handleMessage(Context context) {
        int unreadCount;
        try {
            unreadCount = RequestHelper.getUnreadNum();
        } catch (ConnectionException | RemoteException e) {
            LogUtils.i(TAG, "check notifications count failed", e);
            return;
        }

        if (BuildConfig.DEBUG) {
            unreadCount = new Random().nextInt(20);
        }

        if (unreadCount > 0) {
            AppCtx.getEventBus().post(new NewUnreadEvent(unreadCount));
        } else {
            LogUtils.d(TAG, "not found new unread notifications");
        }
    }
}
