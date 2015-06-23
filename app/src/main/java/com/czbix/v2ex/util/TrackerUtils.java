package com.czbix.v2ex.util;

import android.content.Context;

import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.Map;

public class TrackerUtils {
    private static GoogleAnalytics mAnalytics;
    private static Tracker mTracker;

    public static void init(Context context) {
        mAnalytics = GoogleAnalytics.getInstance(context);
        if (BuildConfig.DEBUG) {
            mAnalytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            mAnalytics.setDryRun(true);
        }

        mTracker = mAnalytics.newTracker(R.xml.analytics_config);

        onAppCreate();
    }

    public static void onAppCreate() {
        final Map<String, String> hit = new HitBuilders.EventBuilder(Category.APP, Action.CREATE).build();
        mTracker.send(hit);
    }

    private static class Category {
        public static final String APP = "App";
    }

    private static class Action {
        public static final String CREATE = "Create";
    }
}
