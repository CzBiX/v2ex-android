package com.czbix.v2ex.service.fcm.message

import android.content.Context
import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.event.BaseEvent.NewUnreadEvent
import com.czbix.v2ex.google.GoogleHelper
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.service.TaskService
import com.czbix.v2ex.util.LogUtils
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Trigger
import java.util.concurrent.TimeUnit

class NotificationFcmMessage : FcmMessage() {
    override fun handleMessage(context: Context) {
        val unreadCount: Int
        try {
            unreadCount = RequestHelper.getUnreadNum()
        } catch (e: ConnectionException) {
            LogUtils.i(TAG, "check notifications count failed", e)
            return
        } catch (e: RemoteException) {
            LogUtils.i(TAG, "check notifications count failed", e)
            return
        }

        if (unreadCount > 0) {
            RxBus.post(NewUnreadEvent(unreadCount))
            scheduleNextCheck(context)
        } else {
            LogUtils.d(TAG, "not found new unread notifications")
        }
    }

    companion object {
        private val TAG = NotificationFcmMessage::class.java.simpleName

        val MSG_TYPE = "notifications"

        internal fun scheduleNextCheck(context: Context) {
            if (!GoogleHelper.isPlayServicesAvailable(context)) {
                LogUtils.d(TAG, "Google Play Services is not available, skip schedule check")
                return
            }

            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))

            val job = dispatcher.newJobBuilder().apply {
                setService(TaskService::class.java)
                tag = TaskService.TASK_NOTIFICATION_CHECK
                isRecurring = true
                trigger = Trigger.executionWindow(TimeUnit.MINUTES.toSeconds(15L).toInt(),
                        TimeUnit.MINUTES.toSeconds(60L).toInt())
                setReplaceCurrent(true)
                setConstraints(Constraint.ON_ANY_NETWORK)
            }.build()

            dispatcher.schedule(job)
        }
    }
}
