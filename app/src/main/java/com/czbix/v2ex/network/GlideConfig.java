package com.czbix.v2ex.network;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;

import java.io.InputStream;

public class GlideConfig implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        int DEFAULT_DISK_CACHE_SIZE = 64 * 1024 * 1024;

        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888)
                .setDiskCache(new InternalCacheDiskCacheFactory(context, DEFAULT_DISK_CACHE_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(RequestHelper.getClient()));
    }
}