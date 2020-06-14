package com.czbix.v2ex.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(message)
        if (t != null) {
            crashlytics.recordException(t)
        }
    }
}

