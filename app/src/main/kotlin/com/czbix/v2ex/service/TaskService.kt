package com.czbix.v2ex.service

import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.event.BaseEvent.NewUnreadEvent
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.service.fcm.message.NotificationFcmMessage
import com.czbix.v2ex.util.ExecutorUtils
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.getLogTag
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService

class TaskService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        ExecutorUtils.submit {
            val success = runTask(params)
            jobFinished(params, !success)
        }

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    fun runTask(params: JobParameters): Boolean {
        val taskTag = params.tag
        when (taskTag) {
            TASK_NOTIFICATION_CHECK -> return checkNotifications()
        }

        LogUtils.w(TAG, "unknown task tag: " + taskTag)
        return false
    }

    private fun checkNotifications(): Boolean {
        val unreadCount: Int
        try {
            unreadCount = RequestHelper.getUnreadNum()
        } catch (e: ConnectionException) {
            LogUtils.i(TAG, "check notifications count failed", e)
            return false
        } catch (e: RemoteException) {
            LogUtils.i(TAG, "check notifications count failed", e)
            return false
        }

        RxBus.post(NewUnreadEvent(unreadCount))
        if (unreadCount != 0) {
            NotificationFcmMessage.scheduleNextCheck(this)
        }

        return true
    }

    companion object {
        val TAG = getLogTag<TaskService>()

        val TASK_NOTIFICATION_CHECK = "notification_check"
    }
}
