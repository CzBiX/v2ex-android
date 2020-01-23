package com.czbix.v2ex.model

import com.czbix.v2ex.db.Member
import com.czbix.v2ex.network.RequestHelper
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.regex.Pattern
import kotlin.properties.Delegates

@Parcelize
data class Topic(
        val id: Int,
        @JvmField
        val title: String?,
        val content: List<ContentBlock>?,
        val replyCount: Int,
        val member: Member?,
        val node: Node?,
        val replyTime: String?,
        val isFavored: Boolean,
        val postscripts: List<Postscript>?
) : Page(), Thankable, Ignorable, Favable {
    @IgnoredOnParcel
    val hasInfo = member != null

    override fun getTitle(): String? {
        return title
    }

    override fun getUrl(): String {
        return buildUrlFromId(id)
    }

    override fun getIgnoreUrl(): String {
        return String.format("%s/ignore/topic/%d", RequestHelper.BASE_URL, id)
    }

    override fun getThankUrl(): String {
        return String.format("%s/thank/topic/%d", RequestHelper.BASE_URL, id)
    }

    override fun getFavUrl(token: String): String {
        return String.format("%s/favorite/topic/%d?t=%s", RequestHelper.BASE_URL, id, token)
    }

    override fun getUnFavUrl(token: String): String {
        return String.format("%s/unfavorite/topic/%d?t=%s", RequestHelper.BASE_URL, id, token)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Topic

        if (id != other.id) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (replyCount != other.replyCount) return false
        if (member != other.member) return false
        if (node != other.node) return false
        if (replyTime != other.replyTime) return false
        if (isFavored != other.isFavored) return false
        if (postscripts != other.postscripts) return false
        if (hasInfo != other.hasInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + replyCount
        result = 31 * result + (member?.hashCode() ?: 0)
        result = 31 * result + (node?.hashCode() ?: 0)
        result = 31 * result + (replyTime?.hashCode() ?: 0)
        result = 31 * result + isFavored.hashCode()
        result = 31 * result + (postscripts?.hashCode() ?: 0)
        result = 31 * result + hasInfo.hashCode()
        return result
    }

    override fun toString(): String {
        return "Topic(id=$id, title=$title)"
    }

    class Builder {
        var id: Int by Delegates.notNull()
        var title: String? = null
        var content: List<ContentBlock>? = null
        var replyCount: Int = 0
        var member: Member? = null
        var node: Node? = null
        var replyTime: String? = null
        var isFavored: Boolean = false
        var postscripts: List<Postscript>? = null

        val hasInfo: Boolean
            get() = member != null

        fun build(): Topic {
            return Topic(
                    id,
                    title,
                    content,
                    replyCount,
                    member,
                    node,
                    replyTime,
                    isFavored,
                    postscripts
            )
        }
    }

    fun toBuilder(): Builder {
        return Builder().also {
            it.id = this.id
            it.title = this.title
            it.content = this.content
            it.replyCount = this.replyCount
            it.member = this.member
            it.node = this.node
            it.replyTime = this.replyTime
            it.isFavored = this.isFavored
            it.postscripts = this.postscripts
        }
    }

    companion object {
        private val PATTERN = Pattern.compile("/t/(\\d+?)(?:\\W|$)")

        fun buildUrlFromId(id: Int): String {
            return RequestHelper.BASE_URL + "/t/" + id.toString()
        }

        @JvmStatic
        fun getIdFromUrl(url: String): Int {
            val matcher = PATTERN.matcher(url)
            if (!matcher.find()) {
                throw IllegalArgumentException("match id for topic failed: $url")
            }
            val idStr = matcher.group(1)
            return Integer.parseInt(idStr)
        }
    }
}
