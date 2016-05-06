package com.czbix.v2ex.common

import com.czbix.v2ex.BuildConfig
import com.czbix.v2ex.event.AppUpdateEvent
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.getLogTag

object UpdateInfo {
    private val TAG = getLogTag<UpdateInfo>()

    var hasNewVersion: Boolean = false
        private set

    fun parseVersionData(info: VersionInfo) {
        val currentVersion = BuildConfig.VERSION_CODE

        if (info.version <= currentVersion) {
            // no new version
            return
        }

        hasNewVersion = true

        LogUtils.i(TAG, "new app version: ${info.version}")
        RxBus.post(AppUpdateEvent())
    }

    data class VersionInfo(
            val version: Int
    )
}
