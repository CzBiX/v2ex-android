package com.czbix.v2ex.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.LogUtils;
import com.czbix.v2ex.util.LruImageCache;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.concurrent.Future;

public class ImageLoader {
    private static final ImageLoader INSTANCE;
    private final LruImageCache mImgCache;

    static {
        INSTANCE = new ImageLoader();
    }

    public static ImageLoader getInstance() {
        return INSTANCE;
    }

    private ImageLoader() {
        mImgCache = new LruImageCache();
    }

    public Future<?> add(int taskId, ImageView view, String url, Callback callback) {
        return ExecutorUtils.submit(new ImageLoadTask(taskId, view, url, callback));
    }

    private static String getCacheKey(String url, int size) {
        return String.format("%s#S%d", url, size);
    }

    private Bitmap getCache(String url, int size) {
        final String key = getCacheKey(url, size);
        return mImgCache.getBitmap(key);
    }

    private void putCache(String url, int size, Bitmap bitmap) {
        final String key = getCacheKey(url, size);
        mImgCache.putBitmap(key, bitmap);
    }

    private class ImageLoadTask implements Runnable {
        private final String TAG = ImageLoadTask.class.getSimpleName();

        private final int mTaskId;
        private final ImageView mView;
        private final String mUrl;
        private final Callback mCallback;

        public ImageLoadTask(int taskId, ImageView view, String url, Callback callback) {
            Preconditions.checkNotNull(view);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(url));
            Preconditions.checkNotNull(callback);

            mTaskId = taskId;
            mView = view;
            mUrl = url;
            mCallback = callback;
        }

        @Override
        public void run() {
            final int size = mView.getHeight();

            Bitmap result = getCache(mUrl, size);
            if (result != null) {
                LogUtils.v(TAG, "image cache hit");
                mCallback.onImgLoadFinish(mTaskId, result);
                return;
            }
            LogUtils.v(TAG, "image cache missed");

            byte[] bytes;
            try {
                bytes = RequestHelper.getImage(mUrl);
            } catch (ConnectionException | RemoteException e) {
                LogUtils.w(TAG, "download image failed", e);
                bytes = null;
            }

            if (bytes == null) {
                result = null;
            } else {
                final Bitmap src = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                result = Bitmap.createScaledBitmap(src, size, size, false);
                putCache(mUrl, size, result);
            }

            mCallback.onImgLoadFinish(mTaskId, result);
        }
    }

    public interface Callback {
        /***
         * it's not called on UI thread.
         */
        void onImgLoadFinish(int taskId, @Nullable Bitmap bitmap);
    }
}
