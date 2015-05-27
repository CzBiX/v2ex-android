package com.czbix.v2ex.ui.loader;

import android.content.Context;

public abstract class AsyncTaskLoader<T> extends android.support.v4.content.AsyncTaskLoader<T> {
    protected T mResult;

    public AsyncTaskLoader(Context context) {
        super(context);
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

        mResult = null;
    }
}
