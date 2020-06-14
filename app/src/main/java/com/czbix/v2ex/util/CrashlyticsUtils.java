package com.czbix.v2ex.util;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class CrashlyticsUtils {
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    public static void setUserState(boolean isLoggedIn) {
        FirebaseCrashlytics.getInstance().setCustomKey(KEY_IS_LOGGED_IN, isLoggedIn);
    }
}
