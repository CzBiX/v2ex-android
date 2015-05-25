package com.czbix.v2ex;

import android.app.Application;

public class AppCtx extends Application {
    private static AppCtx mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static AppCtx getInstance() {
        return mInstance;
    }
}
