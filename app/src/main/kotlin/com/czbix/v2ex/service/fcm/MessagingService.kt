package com.czbix.v2ex.service.fcm

import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.google.GoogleHelper
import com.czbix.v2ex.service.fcm.message.FcmMessage
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.getLogTag
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    val TAG = getLogTag<MessagingService>()

    override fun onMessageReceived(msg: RemoteMessage) {
        val from = msg.from
        val data = msg.data
        LogUtils.v(TAG, "GCM message received, id: %s, from: %s, data: %s", msg.messageId, from, data)

        // the method not run in ui thread, wait context finish init.
        AppCtx.instance.waitUntilInited()

        if (!PrefStore.getInstance().shouldReceiveNotifications()) {
            LogUtils.d(TAG, "should not receive GCM message, unregister it")
            startService(GoogleHelper.getRegistrationIntentToStartService(this, false))
            return
        }

        FcmMessage.from(data).let {
            FcmMessage.handleMessage(this, it)
        }
    }
}
