package com.czbix.v2ex.util

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.widget.Toast

import com.crashlytics.android.Crashlytics
import com.czbix.v2ex.R
import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.ExIllegalStateException
import com.czbix.v2ex.common.exception.FatalException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.common.exception.RequestException
import com.czbix.v2ex.common.exception.UnauthorizedException
import com.czbix.v2ex.network.HttpStatus

import java.util.NoSuchElementException

object ExceptionUtils {
    /**
     * warp exception in [.handleException] with [FatalException]
     */
    @JvmStatic
    fun handleExceptionNoCatch(fragment: Fragment, ex: Exception): Boolean {
        try {
            return handleException(fragment, ex)
        } catch (e: Exception) {
            throw FatalException(e)
        }

    }

    @JvmStatic
    @Throws(Exception::class)
    fun handleException(fragment: Fragment, e: Exception): Boolean {
        val activity = fragment.activity
        var needFinishActivity = false
        val stringId: Int

        when (e) {
            is UnauthorizedException -> {
                needFinishActivity = true
                stringId = R.string.toast_need_sign_in
            }
            is ConnectionException -> {
                stringId = R.string.toast_connection_exception
            }
            is RemoteException -> {
                if (e.code != 504 && e.code != 502) {
                    Crashlytics.logException(e)
                }

                stringId = R.string.toast_remote_exception
            }
            is RequestException -> {
                if (e.isShouldLogged) {
                    Crashlytics.logException(e)
                }
                when (e.code) {
                    HttpStatus.SC_FORBIDDEN -> stringId = R.string.toast_access_denied
                    else -> throw e
                }
            }
            is IllegalStateException -> {
                var logException = true
                if (e is ExIllegalStateException) {
                    logException = e.shouldLogged
                }
                if (logException) {
                    Crashlytics.logException(e)
                } else {
                    Crashlytics.log(e.message)
                }

                stringId = R.string.toast_parse_failed
            }
            is NoSuchElementException, is NullPointerException -> {
                Crashlytics.logException(e)
                stringId = R.string.toast_parse_failed
            }
            else -> {
                throw e
            }
        }

        if (fragment.userVisibleHint) {
            Toast.makeText(activity, stringId, Toast.LENGTH_LONG).show()
        }
        return needFinishActivity
    }
}
