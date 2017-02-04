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
import com.czbix.v2ex.util.result
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Trigger
import java.util.concurrent.TimeUnit

class NotificationFcmMessage : FcmMessage() {
    override fun handleMessage(context: Context) {
        checkNotification(context)
    }

    companion object {
        private val TAG = NotificationFcmMessage::class.java.simpleName

        val MSG_TYPE = "notifications"

        internal fun scheduleNextCheck(context: Context, backoff: Boolean) {
            if (!GoogleHelper.isPlayServicesAvailable(context)) {
                LogUtils.d(TAG, "Google Play Services is not available, skip schedule check")
                return
            }

            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))

            val (minTime, maxTime) = if (backoff) {
                1L to 15L
            } else {
                15L to 60L
            }

            val job = dispatcher.newJobBuilder().apply {
                setService(TaskService::class.java)
                tag = TaskService.TASK_NOTIFICATION_CHECK
                isRecurring = true
                trigger = Trigger.executionWindow(TimeUnit.MINUTES.toSeconds(minTime).toInt(),
                        TimeUnit.MINUTES.toSeconds(maxTime).toInt())
                setReplaceCurrent(true)
                setConstraints(Constraint.ON_ANY_NETWORK)
            }.build()

            dispatcher.schedule(job)
        }

        /**
         * @return is check request successful
         */
        fun checkNotification(context: Context): Boolean {
            val unreadCount: Int
            try {
                unreadCount = RequestHelper.getUnreadNum().result()
            } catch (e: ConnectionException) {
                LogUtils.i(TAG, "check notifications count failed", e)
                scheduleNextCheck(context, true)
                return false
            } catch (e: RemoteException) {
                LogUtils.i(TAG, "check notifications count failed", e)
                scheduleNextCheck(context, true)
                return false
            }

            RxBus.post(NewUnreadEvent(unreadCount))

            if (unreadCount > 0) {
                scheduleNextCheck(context, false)
            } else {
                LogUtils.d(TAG, "not found new unread notifications")
            }

            return true
        }
    }
}
