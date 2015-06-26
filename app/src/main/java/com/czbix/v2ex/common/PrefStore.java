package com.czbix.v2ex.common;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.util.LogUtils;

import java.util.List;

public class PrefStore implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = PrefStore.class.getSimpleName();

    private static final PrefStore instance;
    private static final String PREF_LOAD_IMAGE_ON_MOBILE_NETWORK = "load_image_on_mobile_network";
    public static final String PREF_TABS_TO_SHOW = "tabs_to_show";
    private static final String PREF_RECEIVE_NOTIFICATIONS = "receive_notifications";

    private final SharedPreferences mPreferences;

    static {
        instance = new PrefStore(AppCtx.getInstance());
    }

    PrefStore(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public static void requestBackup() {
        final BackupManager manager = new BackupManager(AppCtx.getInstance());
        manager.dataChanged();
    }

    public static PrefStore getInstance() {
        return instance;
    }

    public boolean isLoadImageOnMobileNetwork() {
        return mPreferences.getBoolean(PREF_LOAD_IMAGE_ON_MOBILE_NETWORK, false);
    }

    public boolean shouldLoadImage() {
        return isLoadImageOnMobileNetwork() || !DeviceStatus.getInstance().isNetworkMetered();
    }

    /**
     * @see Tab#getTabsToShow(String)
     */
    public List<Tab> getTabsToShow() {
        final String string = mPreferences.getString(PREF_TABS_TO_SHOW, null);
        return Tab.getTabsToShow(string);
    }

    public boolean shouldReceiveNotifications() {
        if (UserState.getInstance().isGuest()) {
            LogUtils.v(TAG, "guest can't receive notifications");
            return false;
        }

        return mPreferences.getBoolean(PREF_RECEIVE_NOTIFICATIONS, false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        requestBackup();
    }

    public void unregisterPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public void registerPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }
}
