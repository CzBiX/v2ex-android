package com.czbix.v2ex.presenter;

import android.app.Activity;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextUtils;

import com.czbix.v2ex.helper.CustomTabsHelper;
import com.czbix.v2ex.util.ExecutorUtils;
import com.google.common.base.Strings;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TopicSearchPresenter {
    private static final int DELAY_BEFORE_PREFETCH = 400;
    private final Activity mActivity;

    private CustomTabsHelper mCustomTabsHelper;
    private ScheduledFuture<?> mPrefetchTask;

    public TopicSearchPresenter(Activity activity) {
        mActivity = activity;
        mCustomTabsHelper = new CustomTabsHelper();
    }

    public void start() {
        mCustomTabsHelper.bindCustomTabsService(mActivity);
    }

    public void end() {
        mCustomTabsHelper.unbindCustomTabsService(mActivity);
    }

    public void changeQuery(final String query) {
        if (Strings.isNullOrEmpty(query)) {
            return;
        }

        if (mPrefetchTask != null) {
            mPrefetchTask.cancel(true);
        }

        if (!query.endsWith(" ")) {
            mPrefetchTask = ExecutorUtils.schedule(new Runnable() {
                @Override
                public void run() {
                    mCustomTabsHelper.mayLaunchUrl(getSearchUri(query), null, null);
                }
            }, DELAY_BEFORE_PREFETCH, TimeUnit.MILLISECONDS);
        }
    }

    public boolean submitQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }

        if (mPrefetchTask != null) {
            mPrefetchTask.cancel(true);
        }
        openSearch(query);
        return true;
    }

    private Uri getSearchUri(String query) {
        final String queryToSearch = query + " site:https://www.v2ex.com";

        return new Uri.Builder().scheme("https").authority("www.google.com")
                .path("/search").appendQueryParameter("q", queryToSearch).build();
    }

    private void openSearch(String query) {
        final Uri uri = getSearchUri(query);

        final CustomTabsIntent.Builder builder = CustomTabsHelper.getBuilder(mActivity,
                mCustomTabsHelper.getSession());
        final CustomTabsIntent intent = builder.build();
        intent.launchUrl(mActivity, uri);
    }
}
