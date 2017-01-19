package com.czbix.v2ex.model

import android.os.Parcel
import android.os.Parcelable
import com.czbix.v2ex.common.exception.FatalException
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.ui.widget.ExArrayAdapter
import com.google.common.base.Objects
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.text.Collator
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.regex.Pattern

class Node : Page, Comparable<Node>, ExArrayAdapter.Filterable, Favable {
    val id: Int
    private val mTitle: String?
    val name: String
    val titleAlternative: String?
    val topics: Int
    val avatar: Avatar?
    private val mHasInfo: Boolean

    constructor(title: String?, id: Int, avatar: Avatar?, name: String, alternative: String?,
                topics: Int) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name))
        this.id = id

        mTitle = title
        this.avatar = avatar
        this.name = name
        titleAlternative = alternative
        this.topics = topics

        mHasInfo = !Strings.isNullOrEmpty(title)
    }

    override fun getTitle(): String? {
        return mTitle
    }

    fun hasInfo(): Boolean {
        return mHasInfo
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false
        return Objects.equal(id, other.id) &&
                Objects.equal(topics, other.topics) &&
                Objects.equal(name, other.name) &&
                Objects.equal(titleAlternative, other.titleAlternative) &&
                Objects.equal(avatar, other.avatar)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id, name, titleAlternative, topics, avatar)
    }

    override fun getUrl(): String {
        return buildUrlByName(name)
    }

    override fun filter(query: String): Boolean {
        if (Strings.isNullOrEmpty(query)) {
            return true
        }
        if (name.contains(query) || (mTitle != null && mTitle.contains(query))) {
            return true
        }
        if (titleAlternative != null && titleAlternative.contains(query)) {
            return true
        }
        return false
    }

    override fun compareTo(other: Node): Int {
        return COLLATOR.compare(title, other.title)
    }

    override fun getFavUrl(token: String): String {
        return String.format("%s/favorite/node/%d?once=%s", RequestHelper.BASE_URL, id, token)
    }

    override fun getUnFavUrl(token: String): String {
        return String.format("%s/unfavorite/node/%d?once=%s", RequestHelper.BASE_URL, id, token)
    }

    class Builder {
        private var mId: Int = 0
        private var mTitle: String? = null
        private var mAvatar: Avatar? = null
        private var mName: String? = null
        private var mTitleAlternative: String? = null
        private var mTopics: Int = 0

        fun setId(id: Int): Builder {
            mId = id
            return this
        }

        fun setTitle(title: String): Builder {
            mTitle = title
            return this
        }

        fun setAvatar(avatar: Avatar): Builder {
            mAvatar = avatar
            return this
        }

        fun setName(name: String): Builder {
            mName = name
            return this
        }

        fun setTitleAlternative(titleAlternative: String): Builder {
            mTitleAlternative = titleAlternative
            return this
        }

        fun setTopics(topics: Int): Builder {
            mTopics = topics
            return this
        }

        fun createNode(): Node {
            try {
                val name = checkNotNull(mName)
                if (mTitle == null) {
                    return Node(null, mId, mAvatar, name, mTitleAlternative, mTopics)
                }
                return CACHE[name, { Node(mTitle, mId, mAvatar, name, mTitleAlternative, mTopics) }]
            } catch (e: ExecutionException) {
                throw FatalException(e)
            }

        }

        companion object {
            private val CACHE: Cache<String, Node>

            init {
                CACHE = CacheBuilder.newBuilder()
                        .softValues()
                        .initialCapacity(32)
                        .maximumSize(128)
                        .build<String, Node>()
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.id)
        dest.writeString(this.mTitle)
        dest.writeString(this.name)
        dest.writeString(this.titleAlternative)
        dest.writeInt(this.topics)
        dest.writeParcelable(this.avatar, 0)
        dest.writeByte(if (mHasInfo) 1.toByte() else 0.toByte())
    }

    private constructor(`in`: Parcel) {
        this.id = `in`.readInt()
        this.mTitle = `in`.readString()
        this.name = `in`.readString()
        this.titleAlternative = `in`.readString()
        this.topics = `in`.readInt()
        this.avatar = `in`.readParcelable<Avatar>(Avatar::class.java.classLoader)
        this.mHasInfo = `in`.readByte().toInt() != 0
    }

    companion object {
        private val PATTERN = Pattern.compile("/go/(.+?)(?:\\W|$)")
        private val COLLATOR = Collator.getInstance(Locale.CHINA)

        fun buildUrlByName(name: String): String {
            return RequestHelper.BASE_URL + "/go/" + name
        }

        fun getNameFromUrl(url: String): String {
            val matcher = PATTERN.matcher(url)
            Preconditions.checkState(matcher.find(), "match name for node failed: " + url)
            return matcher.group(1)
        }

        val CREATOR: Parcelable.Creator<Node> = object : Parcelable.Creator<Node> {
            override fun createFromParcel(source: Parcel): Node {
                return Node(source)
            }

            override fun newArray(size: Int): Array<Node?> {
                return arrayOfNulls(size)
            }
        }
    }
}
