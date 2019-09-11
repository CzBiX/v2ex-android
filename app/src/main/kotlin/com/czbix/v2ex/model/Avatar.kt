package com.czbix.v2ex.model

import android.os.Parcelable
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.android.parcel.Parcelize
import java.util.regex.Pattern

@Parcelize
data class Avatar(
        val baseUrl: String
) : Parcelable {
    fun getUrlByPx(size: Int): String {
        val sizeName = when {
            size <= SIZE_MINI -> MINI
            size <= SIZE_NORMAL -> NORMAL
            else -> LARGE
        }

        return getUrl(baseUrl, sizeName)
    }

    class Builder {
        private lateinit var mBaseUrl: String

        fun setUrl(url: String): Builder {
            mBaseUrl = getBaseUrl(url)
            return this
        }

        fun setBaseUrl(url: String): Builder {
            mBaseUrl = url
            return this
        }

        fun build(): Avatar {
            return CACHE.get(mBaseUrl) {
                Avatar(mBaseUrl)
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

        private val PATTERN = Pattern.compile("(mini|normal|large)")
        private const val LARGE = "large"
        private const val NORMAL = "normal"
        private const val MINI = "mini"

        fun getUrl(baseUrl: String, size: String): String {
            return "https:" + String.format(baseUrl, size)
        }

        private fun getBaseUrl(url: String): String {
            return PATTERN.matcher(url).replaceFirst("%s")
        }
    }
}
