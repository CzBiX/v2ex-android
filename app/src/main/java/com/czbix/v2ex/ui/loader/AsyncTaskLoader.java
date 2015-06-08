package com.czbix.v2ex.ui.loader;

import android.content.Context;

import com.czbix.v2ex.util.LogUtils;

import java.util.concurrent.TimeUnit;

public abstract class AsyncTaskLoader<T> extends android.support.v4.content.AsyncTaskLoader<AsyncTaskLoader.LoaderResult<T>> {
    private static final String TAG = AsyncTaskLoader.class.getSimpleName();

    private static final long DEFAULT_UPDATE_THROTTLE = TimeUnit.SECONDS.toMillis(3);
    protected LoaderResult<T> mResult;

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

    /**
     * please override {@link #loadInBackgroundWithException()}
     */
    @Override
    public LoaderResult<T> loadInBackground() {
        LoaderResult<T> loaderResult;
        try {
            T result = loadInBackgroundWithException();
            loaderResult = new LoaderResult<>(result);
        } catch (Exception e) {
            LogUtils.d(TAG, "async task loader has exception", e);
            loaderResult = new LoaderResult<>(e);
        }

        mResult = loaderResult;
        return mResult;
    }

    public abstract T loadInBackgroundWithException() throws Exception;

    /**
     * used to wrap data with exception
     * @param <T> result type
     */
    public static class LoaderResult<T> {
        public final Exception mException;
        public final T mResult;

        public LoaderResult(Exception exception) {
            mException = exception;
            mResult = null;
        }

        public LoaderResult(T result) {
            mResult = result;
            mException = null;
        }

        public boolean hasException() {
            return mException != null;
        }
    }
}
