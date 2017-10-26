package com.czbix.v2ex.common

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.annotation.IntDef
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.R
import com.czbix.v2ex.event.BaseEvent.NewUnreadEvent
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.ui.MainActivity
import com.czbix.v2ex.util.MiscUtils
import com.google.common.eventbus.Subscribe

object NotificationStatus {
    private val mNtfManager: NotificationManagerCompat

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(ID_NOTIFICATIONS.toLong(), ID_APP_UPDATE.toLong())
    annotation class NotificationId

    const val ID_NOTIFICATIONS = 0
    const val ID_APP_UPDATE = 1

    private val context: Context
        get() = AppCtx.instance

    init {
        mNtfManager = NotificationManagerCompat.from(context)

        RxBus.subscribe<NewUnreadEvent> {
            onNewUnread(it)
        }
    }

    fun init() {
        // empty for init
    }

    fun showAppUpdate() {
        val pendingIntent = PendingIntent.getActivity(context, 0, MiscUtils.appUpdateIntent, 0)

        val notification = NotificationCompat.Builder(context).apply {
            setSmallIcon(R.drawable.ic_update_black_24dp)
            setTicker(mContext.getString(R.string.ntf_title_app_update))
            setContentTitle(mContext.getString(R.string.ntf_title_app_update))
            setContentText(mContext.getString(R.string.ntf_desc_new_version_of_app))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            setLocalOnly(true)

            setContentIntent(pendingIntent)
        }.build()

        mNtfManager.notify(ID_APP_UPDATE, notification)
    }

    fun onNewUnread(e: NewUnreadEvent) {
        if (!e.hasNew()) {
            cancelNotification(ID_NOTIFICATIONS)
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.BUNDLE_GOTO, MainActivity.GOTO_NOTIFICATIONS)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val notification = NotificationCompat.Builder(context).apply {
            setSmallIcon(R.drawable.ic_notifications_white_24dp)
            setTicker(mContext.getString(R.string.ntf_title_new_notifications))
            setContentTitle(mContext.getString(R.string.ntf_title_new_notifications))
            setContentText(mContext.getString(R.string.ntf_desc_from_v2ex))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setNumber(e.mCount)
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            setContentIntent(pendingIntent)
        }.build()

        mNtfManager.notify(ID_NOTIFICATIONS, notification)
    }

    fun cancelNotification(@NotificationId id: Int) {
        mNtfManager.cancel(id)
    }
}
