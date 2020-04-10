package com.czbix.v2ex.model

import android.os.Parcelable
import androidx.room.Ignore
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.android.parcel.Parcelize
import java.util.regex.Pattern

@Parcelize
data class Avatar
@JvmOverloads
constructor(
        val baseUrl: String,
        @Ignore
        val isGravatar: Boolean = baseUrl.contains("gravatar")
) : Parcelable {
    fun getUrlByPx(size: Int): String {
        if (isGravatar) {
            return getUrl(baseUrl, size)
        }

        val sizeName = when {
            size <= SIZE_MINI -> MINI
            size <= SIZE_NORMAL -> NORMAL
            else -> LARGE
        }

        return getUrl(baseUrl, sizeName)
    }

    class Builder {
        private lateinit var mBaseUrl: String
        private var isGravatar: Boolean? = null

        fun setUrl(url: String): Builder {
            val pair = parseBaseUrl(url)
            isGravatar = pair.first
            mBaseUrl = pair.second
            return this
        }

        fun setBaseUrl(url: String): Builder {
            mBaseUrl = url
            isGravatar = null
            return this
        }

        fun build(): Avatar {
            return CACHE.get(mBaseUrl) {
                if (isGravatar != null && isGravatar!!) {
                    Avatar(mBaseUrl, true)
                } else {
                    Avatar(mBaseUrl)
                }
            }
        }

        companion object {
            private val CACHE: Cache<String, Avatar>

            init {
                CACHE = CacheBuilder.newBuilder()
                        .softValues()
                        .initialCapacity(32)
                        .maximumSize(128)
                        .build()
            }
        }
    }

    companion object {
        const val SIZE_LARGE = 73
        const val SIZE_NORMAL = 48
        const val SIZE_MINI = 24

        private val OLD_PATTERN = Pattern.compile("(mini|normal|large)")
        private val PATTERN = Pattern.compile("""(?<=s=)\d+""")

        private const val LARGE = "large"
        private const val NORMAL = "normal"
        private const val MINI = "mini"

        fun getUrl(baseUrl: String, size: String): String {
            return "https:" + String.format(baseUrl, size)
        }

        fun getUrl(baseUrl: String, size: Int): String {
            return "https:" + String.format(baseUrl, size)
        }

        private fun parseBaseUrl(url: String): Pair<Boolean, String> {
            val trimmedUrl = url.substringAfter(':')
            val matcher = PATTERN.matcher(trimmedUrl)
            if (matcher.find()) {
                return true to matcher.replaceFirst("%d")
            }

            return false to OLD_PATTERN.matcher(trimmedUrl).replaceFirst("%s")
        }
    }
}
