package com.czbix.v2ex.util;

import android.content.Context;

import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.common.base.Preconditions;

import java.util.Map;

public class TrackerUtils {
    private static GoogleAnalytics mAnalytics;
    private static Tracker mTracker;

    public static void init(Context context) {
        Preconditions.checkState(mAnalytics == null, "can't init twice");

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

    public static void onTopicSwitchReply(boolean isShow) {
        final Map<String, String> hit = new HitBuilders.EventBuilder(Category.TOPIC, Action.SWITCH_REPLY)
                .setLabel(isShow ? Label.SHOW : Label.HIDE).build();
        mTracker.send(hit);
    }

    public static void onTopicReply() {
        final Map<String, String> hit = new HitBuilders.EventBuilder(Category.TOPIC, Action.REPLY).build();
        mTracker.send(hit);
    }

    public static void onSearch() {
        final Map<String, String> hit = new HitBuilders.EventBuilder(Category.APP, Action.SEARCH).build();
        mTracker.send(hit);
    }

    private static class Category {
        public static final String APP = "App";
        public static final String TOPIC = "Topic";
    }

    private static class Action {
        public static final String CREATE = "Create";
        public static final String SWITCH_REPLY = "Switch Reply";
        public static final String REPLY = "Reply";
        public static final String SEARCH = "Search";
    }

    private static class Label {
        public static final String SHOW = "Show";
        public static final String HIDE = "Hide";
    }
}
