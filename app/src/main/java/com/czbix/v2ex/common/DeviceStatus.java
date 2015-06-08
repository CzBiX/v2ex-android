package com.czbix.v2ex.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    private boolean mIsMobileNetwork;

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
        mIsMobileNetwork = checkIsMobileNetwork();
    }

    private boolean checkIsMobileNetwork() {
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected()
                && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public boolean isMobileNetwork() {
        return mIsMobileNetwork;
    }
}
