package com.czbix.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.R;
import com.czbix.v2ex.util.UserUtils;

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
            if (!AppCtx.getInstance().isLoggedIn()) {
                getPreferenceScreen().removePreference(user);
                return;
            }

            final Preference infoPref = findPreference("user_info");
            final Preference logoutPref = findPreference("logout");

            infoPref.setTitle(AppCtx.getInstance().getUsername());
            // TODO: jump to user info page
            infoPref.setEnabled(false);
            logoutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    UserUtils.logout();
                    getActivity().recreate();
                    return true;
                }
            });
        }

        private void initGeneral() {
            final PreferenceCategory general = (PreferenceCategory) findPreference("general");
            final Preference debugPref = findPreference("debug");
            final Preference loginPref = findPreference("login");

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

            if (AppCtx.getInstance().isLoggedIn()) {
                general.removePreference(loginPref);
            } else {
                loginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivityForResult(new Intent(getActivity(), LoginActivity.class), 0);
                        return true;
                    }
                });
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
