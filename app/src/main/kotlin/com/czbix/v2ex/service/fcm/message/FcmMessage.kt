package com.czbix.v2ex.service.fcm.message

import android.content.Context

import com.czbix.v2ex.util.LogUtils
import com.google.common.base.Preconditions
import com.google.common.base.Strings

abstract class FcmMessage {
    /**
     * handle message in a background thread, be thread safe if need
     */
    protected abstract fun handleMessage(context: Context)

    private class UnsupportedFcmMessage(private val type: String?) : FcmMessage() {

        override fun handleMessage(context: Context) {
            LogUtils.d(UnsupportedFcmMessage::class.java, "unsupported FCM message type: %s, do nothing",
                    type)
        }
    }

    companion object {
        fun from(data: Map<String, String>): FcmMessage {
            val type = data["type"]

            return when (type) {
                NotificationFcmMessage.MSG_TYPE -> NotificationFcmMessage()
                else -> UnsupportedFcmMessage(type)
            }
        }

        fun handleMessage(context: Context, message: FcmMessage) {
            message.handleMessage(context)
        }
    }
}
