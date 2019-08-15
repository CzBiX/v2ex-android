package com.czbix.v2ex.network

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.czbix.v2ex.model.loader.GooglePhotoUrlLoader
import java.io.InputStream

@GlideModule
class GlideConfig : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSize: Long = 32 * 1024 * 1024

        builder.apply {
            setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSize))
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(RequestHelper.client))
        registry.prepend(String::class.java, InputStream::class.java, GooglePhotoUrlLoader.Factory())
    }
}
