package com.czbix.v2ex;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.eventbus.BusEvent;
import com.czbix.v2ex.ui.DebugActivity;
import com.czbix.v2ex.ui.LoginActivity;
import com.google.common.base.Strings;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }


    public static class PrefsFragment extends PreferenceFragment {
        private static final int REQ_LOGIN = 0;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general);

            initGeneral();
            initUser();
        }

        private void initUser() {
            final PreferenceCategory user = (PreferenceCategory) findPreference("user");
            final Preference infoPref = findPreference("user_info");
            final Preference loginPref = findPreference("login");
            final Preference logoutPref = findPreference("logout");

            if (Strings.isNullOrEmpty(AppCtx.getInstance().getUsername())) {
                user.removePreference(infoPref);
                user.removePreference(logoutPref);
                loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivityForResult(new Intent(getActivity(), LoginActivity.class), 0);
                        return true;
                    }
                });
            } else {
                infoPref.setTitle(AppCtx.getInstance().getUsername());
                infoPref.setEnabled(false);
                user.removePreference(loginPref);
                logoutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ConfigDao.remove(ConfigDao.KEY_USERNAME);
                        AppCtx.getEventBus().post(new BusEvent.LoginEvent());
                        getActivity().recreate();
                        return true;
                    }
                });
            }
        }

        private void initGeneral() {
            final PreferenceCategory general = (PreferenceCategory) findPreference("general");
            final Preference debugPref = findPreference("debug");
            if (BuildConfig.DEBUG) {
                debugPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getActivity(), DebugActivity.class));
                        return true;
                    }
                });
            } else {
                general.removePreference(debugPref);
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
    }
}
