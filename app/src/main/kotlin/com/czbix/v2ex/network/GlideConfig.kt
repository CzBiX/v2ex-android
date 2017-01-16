package com.czbix.v2ex.network

import android.content.Context

import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.GlideModule

import java.io.InputStream

class GlideConfig : GlideModule {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val DEFAULT_DISK_CACHE_SIZE = 32 * 1024 * 1024

        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888)
                .setDiskCache(InternalCacheDiskCacheFactory(context, DEFAULT_DISK_CACHE_SIZE))
    }

    override fun registerComponents(context: Context, glide: Glide) {
        glide.register(GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(RequestHelper.client))
    }
}
