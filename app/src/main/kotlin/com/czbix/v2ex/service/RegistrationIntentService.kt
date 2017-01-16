package com.czbix.v2ex.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.common.exception.UnauthorizedException
import com.czbix.v2ex.event.DeviceRegisterEvent
import com.czbix.v2ex.google.GoogleHelper
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.network.CzRequestHelper
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.util.LogUtils
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import com.google.common.base.Strings

class RegistrationIntentService : IntentService(TAG) {

    private lateinit var mPreferences: SharedPreferences

    override fun onHandleIntent(intent: Intent?) {
        val isRegister: Boolean
        if (!UserState.isLoggedIn()) {
            // unregister if user logout
            isRegister = false
        } else {
            isRegister = intent == null || !intent.hasExtra(KEY_UNREGISTER)
        }

        mPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        checkPref()

        val isSuccess = if (isRegister) register() else unregister()

        RxBus.post(DeviceRegisterEvent(isRegister, isSuccess))
    }

    private fun checkPref() {
        if (PrefStore.getInstance().shouldClearGcmInfo()) {
            mPreferences.edit().clear().apply()
            PrefStore.getInstance().unsetShouldClearGcmInfo()
        }
    }

    private fun register(): Boolean {
        synchronized(TAG) {
            var token: String? = null
            try {
                // In the (unlikely) event that multiple refresh operations occur simultaneously,
                // ensure that they are processed sequentially.
                val instanceID = InstanceID.getInstance(this)
                token = instanceID.getToken(GoogleHelper.GCM_SENDER_ID,
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE)
                LogUtils.d(TAG, "GCM Registration Token: %s", token)
            } catch (e: Exception) {
                LogUtils.w(TAG, "Failed to complete token refresh", e)
            }

            if (Strings.isNullOrEmpty(token)) {
                return false
            } else if (token == mPreferences.getString(PREF_LAST_GCM_TOKEN, null)) {
                LogUtils.v(TAG, "token already sent to server")
                return true
            }

            try {
                return sendRegistrationToServer(token!!)
            } catch (e: ConnectionException) {
                // TODO: handle network exception
                LogUtils.w(TAG, "register device on server failed", e)
            } catch (e: RemoteException) {
                LogUtils.w(TAG, "register device on server failed", e)
            }

            return false
        }
    }

    private fun unregister(): Boolean {
        synchronized(TAG) {
            val oldToken = mPreferences.getString(PREF_LAST_GCM_TOKEN, null)
            if (Strings.isNullOrEmpty(oldToken)) {
                LogUtils.d(TAG, "not register on server yet")
                return true
            }

            try {
                // never delete token/id on Google, see https://developers.google.com/cloud-messaging/registration#unregistration-and-unsubscription
                return deleteRegistrationOnServer(oldToken)
            } catch (e: Exception) {
                LogUtils.w(TAG, "delete registration on server failed", e)
            }

            return false
        }
    }

    @Throws(ConnectionException::class, RemoteException::class)
    private fun sendRegistrationToServer(gcmToken: String): Boolean {
        val notificationsToken: String
        try {
            notificationsToken = RequestHelper.getNotificationsToken()
        } catch (e: UnauthorizedException) {
            LogUtils.w(TAG, "user signed out, can't send gcm token")
            return false
        }

        val username = UserState.username!!
        if (!mPreferences.getBoolean(PREF_IS_USER_REGISTERED, false)) {
            CzRequestHelper.registerUser(username)
            mPreferences.edit().putBoolean(PREF_IS_USER_REGISTERED, true).apply()
        }

        if (notificationsToken != mPreferences.getString(PREF_LAST_NTF_TOKEN, null)) {
            CzRequestHelper.updateNotificationsToken(username, notificationsToken)
            mPreferences.edit().putString(PREF_LAST_NTF_TOKEN, notificationsToken).apply()
        }

        CzRequestHelper.registerDevice(username, gcmToken)
        mPreferences.edit().putString(PREF_LAST_GCM_TOKEN, gcmToken).apply()

        return true
    }

    @Throws(ConnectionException::class, RemoteException::class)
    private fun deleteRegistrationOnServer(token: String): Boolean {
        val username = UserState.username!!
        if (Strings.isNullOrEmpty(username)) {
            LogUtils.w(TAG, "username is null, can't delete gcm token")
            return false
        }

        CzRequestHelper.unregisterDevice(username, token)
        mPreferences.edit().remove(PREF_LAST_NTF_TOKEN).remove(PREF_LAST_GCM_TOKEN).apply()

        return true
    }

    companion object {
        private val TAG = RegistrationIntentService::class.java.simpleName
        const val KEY_UNREGISTER = "unregister"

        private val PREF_NAME = "gcm"
        private val PREF_IS_USER_REGISTERED = "is_user_registered"
        private val PREF_LAST_GCM_TOKEN = "last_gcm_token"
        private val PREF_LAST_NTF_TOKEN = "last_ntf_token"
    }
}
