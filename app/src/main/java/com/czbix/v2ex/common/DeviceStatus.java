package com.czbix.v2ex.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.net.ConnectivityManagerCompat;

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

    DeviceStatus(Context context) {
        mConnectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateNetworkStatus();
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        updateNetworkStatus();
    }

    private void updateNetworkStatus() {
        mIsNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(mConnectivityManager);
    }

    public boolean isNetworkMetered() {
        return mIsNetworkMetered;
    }
}
