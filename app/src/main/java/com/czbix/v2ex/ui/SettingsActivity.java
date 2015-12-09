package com.czbix.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.R;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.eventbus.gcm.DeviceRegisterEvent;
import com.czbix.v2ex.google.GoogleHelper;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.util.MiscUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;

public class SettingsActivity extends BaseActivity {
    private final PrefsFragment mFragment = new PrefsFragment();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                mFragment).commit();
        final ActionBar actionBar = getSupportActionBar();
        Preconditions.checkNotNull(actionBar);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragment.onActivityResult(requestCode, resultCode, data);
    }

    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private static final String PREF_KEY_CATEGORY_GENERAL = "general";
        private static final String PREF_KEY_CATEGORY_USER = "user";
        private static final String PREF_KEY_USER_INFO = "user_info";
        private static final String PREF_KEY_RECEIVE_NOTIFICATIONS = "receive_notifications";
        private static final String PREF_KEY_LOGIN = "login";
        private static final String PREF_KEY_DEBUG = "debug";
        private static final String PREF_KEY_LOGOUT = "logout";

        private static final int REQ_LOGIN = 0;
        private SwitchPreference mNotificationsPref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general);

            initGeneral();
            initUser();
        }

        private void initUser() {
            final PreferenceCategory user = (PreferenceCategory) findPreference(PREF_KEY_CATEGORY_USER);
            if (!UserState.getInstance().isLoggedIn()) {
                mNotificationsPref = null;
                getPreferenceScreen().removePreference(user);
                return;
            }

            final Preference infoPref = findPreference(PREF_KEY_USER_INFO);
            mNotificationsPref = (SwitchPreference) findPreference(PREF_KEY_RECEIVE_NOTIFICATIONS);
            final Preference logoutPref = findPreference(PREF_KEY_LOGOUT);

            infoPref.setTitle(UserState.getInstance().getUsername());
            infoPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MiscUtils.openUrl(getActivity(), Member.buildUrlFromName(
                            UserState.getInstance().getUsername()));
                    return false;
                }
            });

            mNotificationsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return toggleReceiveNotifications((Boolean) newValue);
                }
            });
            logoutPref.setOnPreferenceClickListener(this);
        }

        @Override
        public void onStart() {
            super.onStart();

            if (mNotificationsPref == null) {
                // user not login yet
                return;
            }

            final String errMsg = GoogleHelper.checkPlayServices(getActivity());
            if (Strings.isNullOrEmpty(errMsg)) {
                mNotificationsPref.setEnabled(true);
                return;
            }
            showPlayServicesErrorToast(errMsg);
            mNotificationsPref.setEnabled(false);
        }

        private void showPlayServicesErrorToast(String errMsg) {
            Toast.makeText(getActivity(),
                    getString(R.string.toast_check_google_play_services_failed, errMsg),
                    Toast.LENGTH_LONG).show();
        }

        private void initGeneral() {
            final PreferenceCategory general = (PreferenceCategory) findPreference(PREF_KEY_CATEGORY_GENERAL);
            final Preference debugPref = findPreference(PREF_KEY_DEBUG);
            final Preference loginPref = findPreference(PREF_KEY_LOGIN);

            if (BuildConfig.DEBUG) {
                debugPref.setOnPreferenceClickListener(this);
            } else {
                general.removePreference(debugPref);
            }

            if (UserState.getInstance().isLoggedIn()) {
                general.removePreference(loginPref);
            } else {
                loginPref.setOnPreferenceClickListener(this);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQ_LOGIN:
                    if (resultCode == RESULT_OK) {
                        getActivity().recreate();
                    }
                    break;
            }

            super.onActivityResult(requestCode, resultCode, data);
        }

        private boolean toggleReceiveNotifications(boolean turnOn) {
            Preconditions.checkState(UserState.getInstance().isLoggedIn(), "guest can't toggle notifications");

            mNotificationsPref.setEnabled(false);
            AppCtx.getEventBus().register(this);
            getActivity().startService(GoogleHelper.getRegistrationIntentToStartService(getActivity(), turnOn));
            return false;
        }

        @Subscribe
        public void onDeviceRegisterEvent(DeviceRegisterEvent e) {
            AppCtx.getEventBus().unregister(this);
            if (!e.isSuccess) {
                Toast.makeText(AppCtx.getInstance(), e.isRegister
                        ? R.string.toast_register_device_failed
                        : R.string.toast_unregister_device_failed, Toast.LENGTH_LONG).show();
                mNotificationsPref.setChecked(e.isRegister);
            }
            mNotificationsPref.setEnabled(true);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case PREF_KEY_LOGIN:
                    startActivityForResult(new Intent(getActivity(), LoginActivity.class), 0);
                    return true;
                case PREF_KEY_LOGOUT:
                    UserState.getInstance().logout();
                    getActivity().recreate();
                    return true;
                case PREF_KEY_DEBUG:
                    startActivity(new Intent(getActivity(), DebugActivity.class));
                    return true;
            }

            return false;
        }
    }
}
