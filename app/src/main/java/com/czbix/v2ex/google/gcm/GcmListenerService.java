package com.czbix.v2ex.google.gcm;

import android.os.Bundle;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.common.PrefStore;
import com.czbix.v2ex.google.GoogleHelper;
import com.czbix.v2ex.google.gcm.message.GcmMessage;
import com.czbix.v2ex.util.LogUtils;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    private static final String TAG = GcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        LogUtils.v(TAG, "GCM message received, from: %s, data: %s", from, data);

        // the method not run in ui thread, wait context finish init.
        AppCtx.getInstance().waitUntilInited();

        if (!PrefStore.getInstance().shouldReceiveNotifications()) {
            LogUtils.d(TAG, "should not receive GCM message, unregister it");
            startService(GoogleHelper.getRegistrationIntentToStartService(this, false));
            return;
        }
        final GcmMessage message = GcmMessage.from(data);
        GcmMessage.handleMessage(this, message);
    }
}
