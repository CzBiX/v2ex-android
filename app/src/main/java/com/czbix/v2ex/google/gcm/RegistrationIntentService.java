package com.czbix.v2ex.google.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.common.exception.UnauthorizedException;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.eventbus.gcm.DeviceRegisterEvent;
import com.czbix.v2ex.google.GoogleHelper;
import com.czbix.v2ex.network.CzRequestHelper;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.LogUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.common.base.Strings;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    public static final String KEY_UNREGISTER = "unregister";

    private static final String PREF_NAME = "gcm";
    private static final String PREF_LAST_GCM_TOKEN = "last_gcm_token";
    private static final String PREF_LAST_NTF_TOKEN = "last_ntf_token";

    private SharedPreferences mPreferences;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final boolean isRegister;
        if (!UserState.getInstance().isLoggedIn()) {
            // unregister if user logout
            isRegister = false;
        } else {
            isRegister = intent == null || !intent.hasExtra(KEY_UNREGISTER);
        }

        mPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        final boolean isSuccess = isRegister ? register() : unregister();

        if (!isSuccess) {
            ExecutorUtils.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppCtx.getInstance(), isRegister
                            ? R.string.toast_register_device_failed
                            : R.string.toast_unregister_device_failed, Toast.LENGTH_LONG).show();
                }
            });
        }
        AppCtx.getEventBus().post(new DeviceRegisterEvent(isRegister, isSuccess));
    }

    private boolean register() {
        synchronized (TAG) {
            String token = null;
            try {
                // In the (unlikely) event that multiple refresh operations occur simultaneously,
                // ensure that they are processed sequentially.
                InstanceID instanceID = InstanceID.getInstance(this);
                token = instanceID.getToken(GoogleHelper.GCM_SENDER_ID,
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                LogUtils.d(TAG, "GCM Registration Token: %s", token);
            } catch (Exception e) {
                LogUtils.w(TAG, "Failed to complete token refresh", e);
            }

            if (Strings.isNullOrEmpty(token)) {
                return false;
            } else if (token.equals(mPreferences.getString(PREF_LAST_GCM_TOKEN, null))) {
                LogUtils.v(TAG, "token already sent to server");
                return true;
            }

            try {
                return sendRegistrationToServer(token);
            } catch (ConnectionException | RemoteException e) {
                // TODO: handle network exception
                LogUtils.w(TAG, "register device on server failed", e);
            }

            return false;
        }
    }

    private boolean unregister() {
        synchronized (TAG) {
            final String oldToken = mPreferences.getString(PREF_LAST_GCM_TOKEN, null);
            if (Strings.isNullOrEmpty(oldToken)) {
                LogUtils.d(TAG, "not register on server yet");
                return true;
            }

            try {
                // never delete token/id on Google, see https://developers.google.com/cloud-messaging/registration#unregistration-and-unsubscription
                return deleteRegistrationOnServer(oldToken);
            } catch (Exception e) {
                LogUtils.w(TAG, "delete registration on server failed", e);
            }

            return false;
        }
    }

    private boolean sendRegistrationToServer(String gcmToken) throws ConnectionException, RemoteException {
        final String notificationsToken;
        try {
            notificationsToken = RequestHelper.getNotificationsToken();
        } catch (UnauthorizedException e) {
            LogUtils.w(TAG, "user signed out, can't send gcm token");
            return false;
        }

        final String username = UserState.getInstance().getUsername();
        if (!ConfigDao.get(ConfigDao.KEY_IS_USER_REGISTERED, false)) {
            CzRequestHelper.registerUser(username);
            ConfigDao.put(ConfigDao.KEY_IS_USER_REGISTERED, true);
        }

        if (!notificationsToken.equals(mPreferences.getString(PREF_LAST_NTF_TOKEN, null))) {
            CzRequestHelper.updateNotificationsToken(username, notificationsToken);
            mPreferences.edit().putString(PREF_LAST_NTF_TOKEN, notificationsToken).apply();
        }

        CzRequestHelper.registerDevice(username, gcmToken);
        mPreferences.edit().putString(PREF_LAST_GCM_TOKEN, gcmToken).apply();

        return true;
    }

    private boolean deleteRegistrationOnServer(String token) throws ConnectionException, RemoteException {
        final String username = UserState.getInstance().getUsername();
        if (Strings.isNullOrEmpty(username)) {
            LogUtils.w(TAG, "username is null, can't delete gcm token");
            return false;
        }

        CzRequestHelper.unregisterDevice(username, token);
        mPreferences.edit().remove(PREF_LAST_NTF_TOKEN).remove(PREF_LAST_GCM_TOKEN).apply();

        return true;
    }
}
