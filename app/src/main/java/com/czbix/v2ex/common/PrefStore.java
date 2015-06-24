package com.czbix.v2ex.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.model.Tab;

import java.util.List;

public class PrefStore {
    private static final PrefStore instance;
    private static final String PREF_LOAD_IMAGE_ON_MOBILE_NETWORK = "load_image_on_mobile_network";
    private static final String PREF_TABS_TO_SHOW = "tabs_to_show";

    private final SharedPreferences mPreferences;

    static {
        instance = new PrefStore(AppCtx.getInstance());
    }

    PrefStore(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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
}
