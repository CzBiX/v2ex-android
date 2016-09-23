package com.czbix.v2ex.common;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.util.LogUtils;

import java.io.File;
import java.util.List;

public class PrefStore implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = PrefStore.class.getSimpleName();
    private static final int VERSION = 3;

    private static PrefStore instance;
    private static final String PREF_LOAD_IMAGE_ON_MOBILE_NETWORK = "load_image_on_mobile_network";
    public static final String PREF_TABS_TO_SHOW = "tabs_to_show";
    public static final String PREF_RECEIVE_NOTIFICATIONS = "receive_notifications";
    private static final String PREF_ALWAYS_SHOW_REPLY_FORM = "always_show_reply_form";
    private static final String PREF_ENABLE_UNDO = "enable_undo";
    private static final String PREF_LAST_PREF_VERSION = "last_app_version";
    private static final String PREF_SHOULD_CLEAR_GCM_INFO = "should_clear_gcm_info";
    @Deprecated
    private static final String PREF_ENABLE_FORCE_TOUCH = "enable_force_touch";
    private static final String PREF_CONTENT_SELECTABLE = "content_selectable";

    private final SharedPreferences mPreferences;

    static {
        instance = new PrefStore(AppCtx.getInstance());
    }

    PrefStore(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        initPref();
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void initPref() {
        int version = mPreferences.getInt(PREF_LAST_PREF_VERSION, 0);

        if (version == VERSION) {
            return;
        }

        // disable force touch
        if (version == 1) {
            //noinspection deprecation
            mPreferences.edit().remove(PREF_ENABLE_FORCE_TOUCH)
                    .putInt(PREF_LAST_PREF_VERSION, 2).apply();
            version = 2;
        }
        if (version == 2) {
            final File path = AppCtx.getInstance().getFileStreamPath("CookiePrefsFile.xml");
            if (path.isFile()) {
                path.delete();
            }
            mPreferences.edit().putInt(PREF_LAST_PREF_VERSION, 3).apply();
            version = 3;
        }

        // HACK: server data loss, force register gcm, remove this line at future
        mPreferences.edit().putBoolean(PREF_SHOULD_CLEAR_GCM_INFO, true).apply();
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
        if (!UserState.getInstance().isLoggedIn()) {
            LogUtils.i(TAG, "Guest can't receive notifications");
            return false;
        }

        return mPreferences.getBoolean(PREF_RECEIVE_NOTIFICATIONS, false);
    }

    public boolean isAlwaysShowReplyForm() {
        return mPreferences.getBoolean(PREF_ALWAYS_SHOW_REPLY_FORM, false);
    }

    public boolean isUndoEnabled() {
        return mPreferences.getBoolean(PREF_ENABLE_UNDO, false);
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

    public boolean shouldClearGcmInfo() {
        return mPreferences.getBoolean(PREF_SHOULD_CLEAR_GCM_INFO, false);
    }

    public void unsetShouldClearGcmInfo() {
        mPreferences.edit().remove(PREF_SHOULD_CLEAR_GCM_INFO).apply();
    }

    public boolean isContentSelectable() {
        return mPreferences.getBoolean(PREF_CONTENT_SELECTABLE, false);
    }
}
