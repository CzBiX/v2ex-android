package com.czbix.v2ex.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.czbix.v2ex.common.exception.ConnectionException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageLoader {
    private static final ImageLoader INSTANCE;

    static {
        INSTANCE = new ImageLoader();
    }

    private final ExecutorService mExecutorService;

    public static ImageLoader getInstance() {
        return INSTANCE;
    }

    public ImageLoader() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    public Future<?> add(int taskId, ImageView view, String url, Callback callback) {
        return mExecutorService.submit(new ImageLoadTask(taskId, view, url, callback));
    }

    private static class ImageLoadTask implements Runnable {
        private static final String TAG = ImageLoadTask.class.getSimpleName();

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
            byte[] bytes;
            try {
                bytes = RequestHelper.getImage(mUrl);
            } catch (ConnectionException | RemoteException e) {
                Log.w(TAG, "download image failed", e);
                bytes = null;
            }

            final Bitmap result;
            if (bytes == null) {
                result = null;
            } else {
                final Bitmap src = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                final int size = mView.getHeight();
                result = Bitmap.createScaledBitmap(src, size, size, false);
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
