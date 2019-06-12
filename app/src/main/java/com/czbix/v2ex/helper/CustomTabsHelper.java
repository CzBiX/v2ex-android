package com.czbix.v2ex.helper;

import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

import com.czbix.v2ex.util.ViewUtils;

import java.util.List;

public class CustomTabsHelper {
    private static final String CHROME_PKG_NAME = "com.android.chrome";

    private CustomTabsSession mCustomTabsSession;
    private CustomTabsClient mClient;
    private CustomTabsServiceConnection mConnection;
    private ConnectionCallback mConnectionCallback;

    /**
     * Unbinds the Activity from the Custom Tabs Service
     * @param activity the activity that is connected to the service
     */
    public void unbindCustomTabsService(Activity activity) {
        if (mClient == null || mConnection == null) return;
        activity.unbindService(mConnection);
        mClient = null;
        mCustomTabsSession = null;
    }

    /**
     * Creates or retrieves an exiting CustomTabsSession
     *
     * @return a CustomTabsSession
     */
    public CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(null);
        }
        return mCustomTabsSession;
    }

    /**
     * Register a Callback to be called when connected or disconnected from the Custom Tabs Service
     */
    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.mConnectionCallback = connectionCallback;
    }

    /**
     * Binds the Activity to the Custom Tabs Service
     * @param activity the activity to be binded to the service
     */
    public boolean bindCustomTabsService(Activity activity) {
        if (mClient != null) return true;

        mConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                mClient = client;
                mClient.warmup(0L);
                if (mConnectionCallback != null) mConnectionCallback.onCustomTabsConnected();
                //Initialize a session as soon as possible.
                getSession();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClient = null;
                if (mConnectionCallback != null) mConnectionCallback.onCustomTabsDisconnected();
            }
        };
        return CustomTabsClient.bindCustomTabsService(activity, CHROME_PKG_NAME, mConnection);
    }

    public static CustomTabsIntent.Builder getBuilder(Activity activity, @Nullable CustomTabsSession session) {
        final int color = ViewUtils.getAttrColor(activity.getTheme(),
                androidx.appcompat.R.attr.colorPrimary);

        final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(session);

        return builder.setShowTitle(true).setToolbarColor(color).enableUrlBarHiding();
    }

    /**
     * @see {@link CustomTabsSession#mayLaunchUrl(Uri, Bundle, List)}
     * @return true if call to mayLaunchUrl was accepted
     */
    public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
        if (mClient == null) return false;

        CustomTabsSession session = getSession();
        if (session == null) return false;

        return session.mayLaunchUrl(uri, extras, otherLikelyBundles);
    }

    /**
     * A Callback for when the service is connected or disconnected. Use those callbacks to
     * handle UI changes when the service is connected or disconnected
     */
    public interface ConnectionCallback {
        /**
         * Called when the service is connected
         */
        void onCustomTabsConnected();

        /**
         * Called when the service is disconnected
         */
        void onCustomTabsDisconnected();
    }
}
