package com.czbix.v2ex.util;

import com.crashlytics.android.Crashlytics;

public class CrashlyticsUtils {
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    public static void setUserState(boolean isLoggedIn) {
        Crashlytics.setBool(KEY_IS_LOGGED_IN, isLoggedIn);
    }
}
