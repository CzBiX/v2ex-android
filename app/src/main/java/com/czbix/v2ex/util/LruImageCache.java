package com.czbix.v2ex.util;

import android.graphics.Bitmap;
import android.util.LruCache;

public class LruImageCache {

    private LruCache<String, Bitmap> mMemoryCache;

    public LruImageCache() {
        // Naive calculation of available memory: Use 1/8th of memory for cache
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public Bitmap getBitmap(String url) {
        return mMemoryCache.get(url);
    }

    public void putBitmap(String url, Bitmap bitmap) {
        mMemoryCache.put(url, bitmap);
    }
}
