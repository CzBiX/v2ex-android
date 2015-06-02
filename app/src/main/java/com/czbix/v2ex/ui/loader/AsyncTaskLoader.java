package com.czbix.v2ex.ui.loader;

import android.content.Context;

import java.util.concurrent.TimeUnit;

public abstract class AsyncTaskLoader<T> extends android.support.v4.content.AsyncTaskLoader<T> {
    private static final long DEFAULT_UPDATE_THROTTLE = TimeUnit.SECONDS.toMillis(3);
    protected T mResult;

    public AsyncTaskLoader(Context context) {
        super(context);

        setUpdateThrottle(DEFAULT_UPDATE_THROTTLE);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (mResult != null && isStarted()) {
            deliverResult(mResult);
        }

        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();

        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        stopLoading();

        mResult = null;
    }
}
