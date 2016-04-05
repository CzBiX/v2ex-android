package com.czbix.v2ex.parser

import com.czbix.v2ex.helper.JsoupObjects
import com.czbix.v2ex.model.Avatar
import com.czbix.v2ex.model.Member
import com.czbix.v2ex.model.Notification
import com.czbix.v2ex.model.Notification.NotificationType
import com.czbix.v2ex.model.Topic
import com.google.common.base.Preconditions
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

object NotificationParser : Parser() {
    private val REGEX_TOKEN = "http://www.v2ex.com/n/(.+).xml".toRegex()
    private val REGEX_UNREAD = "V2EX \\((\\d+)\\)".toRegex()

    @JvmStatic
    fun parseDoc(doc: Document): List<Notification> {
        val box = JsoupObjects(doc).body().child("#Wrapper").child(".content").child(".box").first()
        val list = JsoupObjects(box).child(".cell[id]").child("table").child("tbody").child("tr")

        return list.map { parseNotification(it) }
    }

    @JvmStatic
    fun parseUnreadCount(doc: Document): Int {
        val title = JsoupObjects(doc).head().bfs("title").first()

        return REGEX_UNREAD.matchEntire(title.text())?.let {
            it.groupValues[1].toInt()
        } ?: 0
    }

    private fun parseNotification(element: Element): Notification {
        val builder = Notification.Builder()
        val member = element.child(0).let { parseMember(it) }
        builder.setMember(member)

        element.child(1).let {
            parseInfo(builder, it)
            parseContent(builder, it)
        }

        return builder.createNotification()
    }

    private fun parseContent(builder: Notification.Builder, ele: Element) {
        val payload = JsoupObjects(ele).child(".payload").firstOrNull()
        if (payload == null) {
            // don't have content
            return
        }

        builder.setContent(payload.html())
    }

    private fun parseInfo(builder: Notification.Builder, ele: Element) {
        builder.setTime(parseTime(ele))

        val fadeEle = ele.child(0)
        builder.setType(parseAction(fadeEle))
        builder.setTopic(parseTopic(fadeEle))
    }

    private fun parseTopic(ele: Element): Topic {
        ele.child(1).let {
            val url = it.attr("href")
            val id = Topic.getIdFromUrl(url)
            val title = it.text()

            return Topic.Builder().setId(id).setTitle(title).createTopic()
        }
    }

    @NotificationType
    private fun parseAction(ele: Element): Int {
        val text = ele.textNodes().first().text()

        return when {
            text.contains("在回复") -> Notification.TYPE_REPLY_COMMENT
            text.contains("感谢了你在主题") -> Notification.TYPE_THANK_COMMENT
            text.contains("收藏了你发布的主题") -> Notification.TYPE_FAV_TOPIC
            text.contains("感谢了你发布的主题") -> Notification.TYPE_THANK_TOPIC
            text.contains("在") -> Notification.TYPE_REPLY_TOPIC

            else -> Notification.TYPE_UNKNOWN
        }
    }

    private fun parseTime(ele: Element): String {
        return JsoupObjects.child(ele, ".snow").text().trim()
    }

    @JvmStatic
    fun parseToken(html: String): String {
        val doc = Parser.toDoc(html)
        val ele = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Main").child(".box:last-child").dfs(".sll").one

        REGEX_TOKEN.matchEntire(ele.`val`())!!.let {
            return it.groupValues[1]
        }
    }
}
