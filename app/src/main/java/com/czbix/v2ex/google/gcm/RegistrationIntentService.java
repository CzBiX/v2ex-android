package com.czbix.v2ex.google.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.google.GoogleHelper;
import com.czbix.v2ex.util.LogUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    public static final String KEY_UNREGISTER = "unregister";

    private static final String PREF_NAME = "gcm";
    private static final String PREF_LAST_SENT_TOKEN = "last_sent_token";

    private SharedPreferences mPreferences;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Preconditions.checkState(!UserState.getInstance().isGuest(), "guest user can't do action with gcm");

        mPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (intent != null && intent.hasExtra(KEY_UNREGISTER)) {
            unregister();
        } else {
            register();
        }
    }

    private void register() {
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

            final String oldToken = mPreferences.getString(PREF_LAST_SENT_TOKEN, null);
            if (Strings.isNullOrEmpty(token)) {
                mPreferences.edit().remove(PREF_LAST_SENT_TOKEN);
            } else if (token.equals(oldToken)) {
                LogUtils.v(TAG, "token already sent to server");
            } else {
                sendRegistrationToServer(token);
                mPreferences.edit().putString(PREF_LAST_SENT_TOKEN, token).apply();
                LogUtils.v(TAG, "updated token on server");
            }
        }

        // TODO: notify others
    }

    private void unregister() {
        boolean success;
        synchronized (TAG) {
            if (Strings.isNullOrEmpty(mPreferences.getString(PREF_LAST_SENT_TOKEN, null))) {
                LogUtils.d(TAG, "not register on server yet");
                return;
            }

            try {
                // never delete token/id on Google, see https://developers.google.com/cloud-messaging/registration#unregistration-and-unsubscription
                deleteRegistrationOnServer();
                success = true;
            } catch (Exception e) {
                LogUtils.w(TAG, "delete registration on server failed", e);
                success = false;
            }
            // TODO: notify others
        }
    }

    private void sendRegistrationToServer(String token) {
        // TODO:
    }

    private void deleteRegistrationOnServer() {

    }
}
