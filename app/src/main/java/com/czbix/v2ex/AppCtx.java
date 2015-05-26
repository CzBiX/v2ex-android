package com.czbix.v2ex;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class AppCtx extends Application {
    private static final String TAG = AppCtx.class.getSimpleName();

    private static AppCtx mInstance;
    private float mDensity;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        mDensity = getDensity(this);
    }

    private static float getDensity(Context context) {
        final float density = context.getResources().getDisplayMetrics().density;

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "density: " + density);
        }

        return density;
    }

    public static AppCtx getInstance() {
        return mInstance;
    }

    public float getDensity() {
        return mDensity;
    }
}
