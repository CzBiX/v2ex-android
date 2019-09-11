package com.czbix.v2ex.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.czbix.v2ex.model.Ignorable
import com.czbix.v2ex.model.Thankable
import com.czbix.v2ex.network.RequestHelper

@Entity
data class Comment(
        @PrimaryKey
        val id: Int,
        @ColumnInfo(index = true)
        val topicId: Int,
        val page: Int,
        val content: String,
        val username: String,
        val addAt: String,
        val thanks: Int,
        val floor: Int,
        val thanked: Boolean
) : Thankable, Ignorable {

    override fun getIgnoreUrl(): String {
        return String.format("%s/ignore/reply/%d", RequestHelper.BASE_URL, id)
    }

    override fun getThankUrl(): String {
        return String.format("%s/thank/reply/%d", RequestHelper.BASE_URL, id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Comment

        if (id != other.id) return false
        if (topicId != other.topicId) return false
        if (page != other.page) return false
        if (content != other.content) return false
        if (username != other.username) return false
        if (addAt != other.addAt) return false
        if (thanks != other.thanks) return false
        if (floor != other.floor) return false
        if (thanked != other.thanked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + topicId
        result = 31 * result + page
        result = 31 * result + content.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + addAt.hashCode()
        result = 31 * result + thanks
        result = 31 * result + floor
        result = 31 * result + thanked.hashCode()
        return result
    }

    class Builder {
        var topicId: Int = 0
        var id: Int = 0
        var page: Int = 0
        lateinit var content: String
        lateinit var username: String
        lateinit var addAt: String
        var thanks: Int = 0
        var floor: Int = 0
        var thanked: Boolean = false

        fun build(): Comment {
            check(topicId > 0)
            check(id > 0)
            check(floor > 0)
            check(page > 0)

            return Comment(id, topicId, page, content, username, addAt, thanks, floor, thanked)
        }
    }
}
