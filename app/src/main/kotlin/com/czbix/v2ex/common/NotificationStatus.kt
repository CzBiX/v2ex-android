package com.czbix.v2ex.common

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.annotation.IntDef
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.R
import com.czbix.v2ex.eventbus.BaseEvent.NewUnreadEvent
import com.czbix.v2ex.ui.MainActivity
import com.czbix.v2ex.util.MiscUtils
import com.google.common.eventbus.Subscribe

class NotificationStatus internal constructor(private val mContext: Context) {

    private val mNtfManager: NotificationManagerCompat

    init {
        mNtfManager = NotificationManagerCompat.from(mContext)
    }

    fun init() {
        AppCtx.eventBus.register(this)
    }

    fun showAppUpdate() {
        val pendingIntent = PendingIntent.getActivity(mContext, 0, MiscUtils.appUpdateIntent, 0)

        val builder = NotificationCompat.Builder(mContext).apply {
            setSmallIcon(R.drawable.ic_update_black_24dp)
            setTicker(mContext.getString(R.string.ntf_title_app_update))
            setContentTitle(mContext.getString(R.string.ntf_title_app_update))
            setContentText(mContext.getString(R.string.ntf_desc_new_version_of_app))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            setLocalOnly(true)

            setContentIntent(pendingIntent)
        }

        mNtfManager.notify(ID_APP_UPDATE, builder.build())
    }

    @Subscribe
    fun onNewUnread(e: NewUnreadEvent) {
        if (!e.hasNew()) {
            cancelNotification(ID_NOTIFICATIONS)
            return
        }

        val intent = Intent(mContext, MainActivity::class.java).apply {
            putExtra(MainActivity.BUNDLE_GOTO, MainActivity.GOTO_NOTIFICATIONS)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(mContext).apply {
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
        }

        mNtfManager.notify(ID_NOTIFICATIONS, builder.build())
    }

    fun cancelNotification(@NotificationId id: Int) {
        mNtfManager.cancel(id)
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(ID_NOTIFICATIONS.toLong())
    annotation class NotificationId

    companion object {
        @JvmStatic
        val instance: NotificationStatus

        const val ID_NOTIFICATIONS = 0
        const val ID_APP_UPDATE = 1

        init {
            instance = NotificationStatus(AppCtx.instance)
        }
    }
}
