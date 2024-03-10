package com.czbix.v2ex.parser

import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.FatalException
import com.czbix.v2ex.db.Member
import com.czbix.v2ex.helper.JsoupObjects
import com.czbix.v2ex.model.*
import com.czbix.v2ex.ui.loader.TopicListLoader
import com.google.common.base.Preconditions
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.util.regex.Pattern

object TopicListParser : Parser() {
    private val PATTERN_REPLY_TIME = Pattern.compile("•\\s*(.+?)(?:\\s+•|$)")

    @JvmStatic
    fun parseDoc(doc: Document, page: Page): TopicListLoader.TopicList {
        val main = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Main").first()

        return when (page) {
            is Node -> parseDocForNode(main, page)
            is Tab, Page.PAGE_FAV_TOPIC -> parseDocForTab(JsoupObjects(main).child(".box:not(.box-title)").first())
            else -> throw IllegalArgumentException("Unknown page type: $page")
        }
    }

    private fun parseDocForTab(contentBox: Element): TopicListLoader.TopicList {
        val elements = JsoupObjects(contentBox).child(".item").child("table").child("tbody").child("tr")
        return elements.map {
            parseItemForTab(it)
        }.let {
            TopicListLoader.TopicList(it, false)
        }
    }

    private fun parseDocForNode(main: Element, node: Node): TopicListLoader.TopicList {
        val (favorited, once) = parseFavorited(main)
        val elements = JsoupObjects(main).child(".box").child("#TopicsNode").child(".cell").child("table").child("tbody").child("tr")
        return elements.map {
            parseItemForNode(it, node)
        }.let {
            TopicListLoader.TopicList(it, favorited, once)
        }
    }

    private fun parseFavorited(main: Element): Pair<Boolean, String?> {
        if (!UserState.isLoggedIn()) {
            return false to null
        }

        val a = JsoupObjects(main).child(".node-header", ".cell_ops", "div", "a").first()
        val href = a.attr("href")

        return href.startsWith("/unfav") to href.substringAfterLast("?once=")
    }

    private fun parseItemForTab(item: Element): Topic {
        val list = item.children()

        val topicBuilder = Topic.Builder()
        parseMember(topicBuilder, list[0])

        val ele = list[2]
        parseTitle(topicBuilder, ele)
        parseInfo(topicBuilder, ele, null)

        parseReplyCount(topicBuilder, list[3])

        return topicBuilder.build()
    }

    private fun parseItemForNode(item: Element, node: Node): Topic {
        val list = item.children()

        val topicBuilder = Topic.Builder()
        parseMember(topicBuilder, list[0])

        val ele = list[2]
        parseTitle(topicBuilder, ele)
        parseInfo(topicBuilder, ele, node)

        parseReplyCount(topicBuilder, list[3])

        return topicBuilder.build()
    }

    private fun parseReplyCount(topicBuilder: Topic.Builder, ele: Element) {
        val children = ele.children()
        val count: Int
        if (children.size > 0) {
            val numStr = ele.child(0).text()
            count = Integer.parseInt(numStr)
        } else {
            // do not have reply yet
            count = 0
        }
        topicBuilder.replyCount = count
    }

    private fun parseInfo(topicBuilder: Topic.Builder, ele: Element, node: Node?) {
        @Suppress("NAME_SHADOWING")
        var node = node
        val topicInfoEle = JsoupObjects.child(ele, ".topic_info, .fade")

        val hasNode: Boolean
        if (node == null) {
            hasNode = false
            node = parseNode(JsoupObjects.child(topicInfoEle, ".node"))
        } else {
            hasNode = true
        }
        topicBuilder.node = node

        val index = if (hasNode) 0 else 1
        if (topicInfoEle.textNodes().size > index) {
            parseReplyTime(topicBuilder, topicInfoEle.textNodes()[index])
        } else {
            // reply time may not exists
            topicBuilder.replyTime = ""
        }
    }

    private fun parseReplyTime(topicBuilder: Topic.Builder, textNode: TextNode) {
        val text = textNode.text()
        val matcher = PATTERN_REPLY_TIME.matcher(text)
        if (!matcher.find()) {
            throw FatalException("Match reply time for topic failed: $text")
        }
        val time = matcher.group(1)
        topicBuilder.replyTime = time
    }

    private fun parseTitle(topicBuilder: Topic.Builder, ele: Element) {
        val a = JsoupObjects(ele).child(".item_title").child("a").first()
        val url = a.attr("href")

        topicBuilder.id = Topic.getIdFromUrl(url)
        topicBuilder.title = a.html()
    }

    internal fun parseMember(builder: Topic.Builder, ele: Element) {
        val memberBuilder = Member.Builder()

        // get member url
        val a = JsoupObjects.child(ele, "a")
        val url = a.attr("href")
        memberBuilder.username = Member.getNameFromUrl(url)

        // get member avatar
        val avatarBuilder = Avatar.Builder()
        val img = JsoupObjects(a).child("img").firstOrNull()
        if (img != null) {
            avatarBuilder.setUrl(img.attr("src"))
        } else {
            // sometimes avatar are missing here
            avatarBuilder.setUrl("https://cdn.v2ex.com/static/img/avatar_large.png")
        }
        memberBuilder.avatar = avatarBuilder.build()

        builder.member = memberBuilder.build()
    }
}
