package com.czbix.v2ex.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.core.net.ConnectivityManagerCompat;

import com.czbix.v2ex.AppCtx;

public class DeviceStatus {
    private static final DeviceStatus instance;

    private final ConnectivityManager mConnectivityManager;

    static {
        instance = new DeviceStatus(AppCtx.getInstance());
    }

    public static DeviceStatus getInstance() {
        return instance;
    }

    private boolean mIsNetworkMetered;
    private boolean mIsNetworkConnected;

    DeviceStatus(Context context) {
        mConnectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateNetworkStatus(intent);
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        updateNetworkStatus(null);
    }

    private void updateNetworkStatus(Intent intent) {
        mIsNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(mConnectivityManager);

        if (intent == null) {
            final NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo == null) {
                mIsNetworkConnected = false;
            } else {
                mIsNetworkConnected = activeNetworkInfo.isConnected();
            }
        } else {
            mIsNetworkConnected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        }
    }

    public boolean isNetworkMetered() {
        return mIsNetworkMetered;
    }

    public boolean isNetworkConnected() {
        return mIsNetworkConnected;
    }
}
