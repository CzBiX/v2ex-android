package com.czbix.v2ex.service

import com.czbix.v2ex.service.fcm.message.NotificationFcmMessage
import com.czbix.v2ex.util.ExecutorUtils
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.getLogTag
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

    private fun runTask(params: JobParameters): Boolean {
        val taskTag = params.tag
        when (taskTag) {
            TASK_NOTIFICATION_CHECK -> return NotificationFcmMessage.checkNotification(this)
        }

        LogUtils.w(TAG, "Unknown task tag: $taskTag")
        return false
    }

    companion object {
        val TAG = getLogTag<TaskService>()

        const val TASK_NOTIFICATION_CHECK = "notification_check"
    }
}
