package com.czbix.v2ex.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.czbix.v2ex.AppCtx;

public class PrefStore {
    private static final PrefStore instance;
    private static final String PREF_LOAD_IMAGE_ON_MOBILE_NETWORK = "load_image_on_mobile_network";

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
}
