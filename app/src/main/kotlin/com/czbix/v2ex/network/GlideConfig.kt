package com.czbix.v2ex.network

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.module.AppGlideModule
import com.czbix.v2ex.model.loader.GooglePhotoUrlLoader
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.getLogTag
import java.io.InputStream
import java.security.MessageDigest
import kotlin.math.min

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

    class AtWidthMostStrategy(private val maxWidth: Int) : DownsampleStrategy() {
        override fun getScaleFactor(sourceWidth: Int, sourceHeight: Int, requestedWidth: Int, requestedHeight: Int): Float {
            val maxWidth = if (maxWidth == 0) requestedWidth else maxWidth
            val fitWidth = min(sourceWidth, maxWidth)

            return fitWidth.toFloat() / sourceWidth
        }

        override fun getSampleSizeRounding(sourceWidth: Int, sourceHeight: Int, requestedWidth: Int, requestedHeight: Int): SampleSizeRounding {
            return SampleSizeRounding.QUALITY
        }
    }

    class AtWidthMostTransformation(private val strategy: DownsampleStrategy) : BitmapTransformation() {
        override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int,
                               outHeight: Int): Bitmap {
            val srcWidth = toTransform.width
            val srcHeight = toTransform.height
            val scaleFactor = strategy.getScaleFactor(srcWidth, srcHeight, outWidth, outHeight)
            if (scaleFactor == 1f) {
                return toTransform
            }

            return Bitmap.createScaledBitmap(
                    toTransform,
                    (srcWidth * scaleFactor).toInt(),
                    (srcHeight * scaleFactor).toInt(),
                    true
            )
        }

        override fun equals(other: Any?): Boolean {
            return other is AtWidthMostTransformation
        }

        override fun hashCode(): Int {
            return ID.hashCode()
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(ID_BYTES)
        }

        companion object {
            private const val ID = "com.bumptech.glide.load.resource.bitmap.FitCenter"
            private val ID_BYTES = ID.toByteArray(Key.CHARSET)
        }
    }

    companion object {
        private val TAG = getLogTag<GlideConfig>()
        private var lastMaxWidth = -1
        private lateinit var lastStrategy : DownsampleStrategy

        fun atWidthMost(maxWidth: Int = 0): DownsampleStrategy {
            if (maxWidth != lastMaxWidth) {
                LogUtils.v(TAG, "Downsample strategy cache missed. Changed from %d to %d.", lastMaxWidth, maxWidth)

                lastMaxWidth = maxWidth
                lastStrategy = AtWidthMostStrategy(maxWidth)
            }

            return lastStrategy
        }
    }
}
