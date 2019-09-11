package com.czbix.v2ex.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.czbix.v2ex.common.exception.FatalException
import com.czbix.v2ex.model.Avatar
import com.czbix.v2ex.model.Page
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.ExecutionException
import java.util.regex.Pattern

@Entity
@Parcelize
data class Member(
        @PrimaryKey
        val username: String,
        @Embedded
        val avatar: Avatar
) : Page() {

    override fun getTitle(): String? {
        return username
    }

    override fun getUrl(): String {
        return buildUrlFromName(username)
    }

    fun isSameUser(member: Member): Boolean {
        return this.username == member.username
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Member

        if (username != other.username) return false
        if (avatar != other.avatar) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + avatar.hashCode()
        return result
    }

    companion object {
        private val PATTERN = Pattern.compile("/member/(.+?)(?:\\W|$)")

        @JvmStatic
        fun getNameFromUrl(url: String): String {
            val matcher = PATTERN.matcher(url)
            if (!matcher.find()) {
                throw FatalException("Match name for member failed: $url")
            }
            return matcher.group(1)
        }

        fun buildUrlFromName(username: String): String {
            return "/member/$username"
        }
    }

    class Builder {
        lateinit var username: String
        lateinit var avatar: Avatar

        fun build(): Member {
            try {
                return CACHE.get(username) {
                    Member(username, avatar)
                }
            } catch (e: ExecutionException) {
                throw FatalException(e)
            }

        }

        companion object {
            private val CACHE: Cache<String, Member>

            init {
                CACHE = CacheBuilder.newBuilder()
                        .initialCapacity(32)
                        .maximumSize(128)
                        .softValues()
                        .build<String, Member>()
            }
        }
    }
}
