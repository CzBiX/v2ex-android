package com.czbix.v2ex.google.gcm;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.event.BaseEvent.NewUnreadEvent;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.util.LogUtils;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;

public class GcmTaskService extends com.google.android.gms.gcm.GcmTaskService {
    public static final String TASK_NOTIFICATION_CHECK = "notification_check";

    @Override
    public int onRunTask(TaskParams taskParams) {
        // the method not run in ui thread, wait context finish init.
        AppCtx.getInstance().waitUntilInited();

        String taskTag = taskParams.getTag();
        switch (taskTag) {
            case TASK_NOTIFICATION_CHECK:
                return checkNotifications();
            default:
                break;
        }

        LogUtils.w(GcmTaskService.class, "unknown task tag: " + taskTag);
        return GcmNetworkManager.RESULT_FAILURE;
    }

    private int checkNotifications() {
        int unreadCount;
        try {
            unreadCount = RequestHelper.getUnreadNum();
        } catch (ConnectionException | RemoteException e) {
            LogUtils.i(GcmTaskService.class, "check notifications count failed", e);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        AppCtx.getEventBus().post(new NewUnreadEvent(unreadCount));
        if (unreadCount == 0) {
            GcmNetworkManager.getInstance(this)
                    .cancelTask(TASK_NOTIFICATION_CHECK, GcmTaskService.class);
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
