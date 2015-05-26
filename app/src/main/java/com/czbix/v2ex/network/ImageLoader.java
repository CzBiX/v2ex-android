package com.czbix.v2ex.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ImageView;

import com.czbix.v2ex.common.exception.ConnectionException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public void add(ImageView view, String url) {
        mExecutorService.submit(new ImageLoadTask(view, url));
    }

    private static class ImageLoadTask implements Runnable {
        private static final String TAG = ImageLoadTask.class.getSimpleName();

        private final ImageView mView;
        private final String mUrl;

        public ImageLoadTask(ImageView view, String url) {
            this.mView = view;
            this.mUrl = url;
        }

        @Override
        public void run() {
            final byte[] bytes;
            try {
                bytes = RequestHelper.getImage(mUrl);
            } catch (ConnectionException | RemoteException e) {
                Log.w(TAG, "download image failed", e);
                return;
            }

            final Bitmap src = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            final int size = mView.getHeight();
            final Bitmap dst = Bitmap.createScaledBitmap(src, size, size, false);

            mView.post(new Runnable() {
                @Override
                public void run() {
                    mView.setImageBitmap(dst);
                }
            });
        }
    }
}
