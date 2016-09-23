package com.czbix.v2ex.google.gcm.message;

import android.content.Context;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.event.BaseEvent.NewUnreadEvent;
import com.czbix.v2ex.google.GoogleHelper;
import com.czbix.v2ex.google.gcm.GcmTaskService;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.util.LogUtils;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;

import java.util.concurrent.TimeUnit;

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

        if (unreadCount > 0) {
            AppCtx.getEventBus().post(new NewUnreadEvent(unreadCount));
            scheduleNextCheck(context);
        } else {
            LogUtils.d(TAG, "not found new unread notifications");
        }
    }

    private void scheduleNextCheck(Context context) {
        if (!GoogleHelper.isPlayServicesAvailable(context)) {
            LogUtils.d(TAG, "Google Play Services is not available, skip schedule check");
            return;
        }

        PeriodicTask task = new PeriodicTask.Builder()
                .setService(GcmTaskService.class)
                .setTag(GcmTaskService.TASK_NOTIFICATION_CHECK)
                .setPeriod(TimeUnit.MINUTES.toSeconds(15))
                .setPersisted(false)
                .setRequiresCharging(false)
                .build();

        GcmNetworkManager.getInstance(context).schedule(task);
    }
}
