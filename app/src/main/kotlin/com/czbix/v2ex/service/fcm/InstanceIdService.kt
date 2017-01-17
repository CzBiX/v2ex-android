package com.czbix.v2ex.service.fcm

import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.google.GoogleHelper
import com.google.firebase.iid.FirebaseInstanceIdService

class InstanceIdService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        startService(GoogleHelper.getRegistrationIntentToStartService(this,
                PrefStore.getInstance().shouldReceiveNotifications()));
    }
}
