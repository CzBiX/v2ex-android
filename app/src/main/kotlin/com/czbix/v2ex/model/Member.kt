package com.czbix.v2ex.model

import android.os.Parcel
import android.os.Parcelable
import com.czbix.v2ex.common.exception.FatalException
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.ExecutionException
import java.util.regex.Pattern

class Member : Page {
    val username: String?
    val tagLine: String?
    val avatar: Avatar?

    constructor(username: String?, avatar: Avatar?, tagLine: String?) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username))

        this.username = username
        this.avatar = avatar
        this.tagLine = tagLine
    }

    override fun getTitle(): String? {
        return username
    }

    override fun getUrl(): String {
        val username = checkNotNull(username)
        return buildUrlFromName(username)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.username)
        dest.writeString(this.tagLine)
        dest.writeParcelable(this.avatar, 0)
    }

    fun isSameUser(member: Member): Boolean {
        return this.username == member.username
    }

    private constructor(`in`: Parcel) {
        this.username = `in`.readString()
        this.tagLine = `in`.readString()
        this.avatar = `in`.readParcelable<Avatar>(Avatar::class.java.classLoader)
    }

    companion object {
        private val PATTERN = Pattern.compile("/member/(.+?)(?:\\W|$)")

        @JvmStatic
        fun getNameFromUrl(url: String): String {
            val matcher = PATTERN.matcher(url)
            if (!matcher.find()) {
                throw FatalException("match name for member failed: " + url)
            }
            return matcher.group(1)
        }

        fun buildUrlFromName(username: String): String {
            return "/member/" + username
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Member> = object : Parcelable.Creator<Member> {
            override fun createFromParcel(source: Parcel): Member {
                return Member(source)
            }

            override fun newArray(size: Int): Array<Member?> {
                return arrayOfNulls(size)
            }
        }
    }

    class Builder {
        private var mUsername: String? = null
        private var mAvatar: Avatar? = null
        private var mTagLine: String? = null

        fun setUsername(title: String): Builder {
            mUsername = title
            return this
        }

        fun setAvatar(avatar: Avatar): Builder {
            mAvatar = avatar
            return this
        }

        fun setTagLine(tagLine: String): Builder {
            mTagLine = tagLine
            return this
        }

        fun createMember(): Member {
            try {
                return CACHE[mUsername!!, { Member(mUsername, mAvatar, mTagLine) }]
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
