package com.czbix.v2ex.model.loader

import android.annotation.SuppressLint
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader
import java.io.InputStream
import kotlin.random.Random

class GooglePhotoUrlLoader constructor(concreteLoader: ModelLoader<GlideUrl, InputStream>) : BaseGlideUrlLoader<String>(concreteLoader) {
    @SuppressLint("DefaultLocale")
    override fun getUrl(s: String, width: Int, height: Int, options: Options): String {
        return String.format("https://lh%d.%s=w%d-h%d", Random.nextInt(3,7), s, width, height)
    }

    override fun handles(s: String): Boolean {
        return s.startsWith("ggpht.com/")
    }

    class Factory : ModelLoaderFactory<String, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
            return GooglePhotoUrlLoader(multiFactory.build(GlideUrl::class.java, InputStream::class.java))
        }

        override fun teardown() {
        }
    }
}
