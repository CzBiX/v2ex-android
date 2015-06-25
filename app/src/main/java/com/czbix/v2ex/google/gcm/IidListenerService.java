package com.czbix.v2ex.google.gcm;

import com.czbix.v2ex.common.PrefStore;
import com.czbix.v2ex.google.GoogleHelper;
import com.google.android.gms.iid.InstanceIDListenerService;

public class IidListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        if (!PrefStore.getInstance().shouldReceiveNotifications()) {
            return;
        }

        startService(GoogleHelper.getRegistrationIntentToStartService(this, true));
    }
}
