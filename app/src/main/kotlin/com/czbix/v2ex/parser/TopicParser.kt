package com.czbix.v2ex.parser

import com.crashlytics.android.Crashlytics
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.helper.JsoupObjects
import com.czbix.v2ex.model.*
import com.google.common.base.Preconditions
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object TopicParser : Parser() {
    private val PATTERN_TOPIC_REPLY_TIME = "at (.+?),".toRegex()
    private val PATTERN_POSTSCRIPT = "·\\s+(.+)".toRegex()
    private val PATTERN_NUMBERS = "\\d+".toRegex()

    @JvmStatic
    fun parseDoc(doc: Document, topic: Topic): TopicWithComments {
        val topicBuilder = topic.toBuilder()
        val contentEle = JsoupObjects(doc).bfs("body").child("#Wrapper").child(".content").first()

        val csrfToken = parseTopicInfo(topicBuilder, contentEle)
        val comments = parseComments(contentEle)
        val pageNum = getMaxPage(contentEle)

        val onceToken = if (UserState.isLoggedIn()) {
            parseOnceToken(doc)
        } else {
            null
        }

        return TopicWithComments(topicBuilder.createTopic(), comments, pageNum.first,
                pageNum.second, csrfToken, onceToken)
    }

    private fun getMaxPage(parent: Element): Pair<Int, Int> {
        val ele = JsoupObjects(parent).child(".box:nth-child(3):not(.transparent)").child(".inner:last-child:not([id])").firstOrNull()
        return if (ele == null) {
            1 to 1
        } else {
            val maxPage = ele.children().size
            val curPage = JsoupObjects.child(ele, ".page_current").text().toInt()

            curPage to maxPage
        }
    }

    private fun parseOnceToken(parent: Element): String? {
        return JsoupObjects(parent).child(".box:nth-child(5)")
                .child(".cell:nth-child(2)").child("form").child("[name=once]")
                .firstOrNull()?.let(Element::`val`)
    }

    private fun parseFavored(builder: Topic.Builder, box: Element): String {
        val ele = JsoupObjects(box).child(".inner").child(".fr").child(".op").first()

        val href = ele.attr("href")
        href.startsWith("/unfav").let { builder.isFavored(it) }

        return href.substringAfterLast("?t=")
    }

    private fun parseTopicInfo(builder: Topic.Builder, parent: Element): String? {
        val topicBox = JsoupObjects.child(parent, ".box")
        val header = JsoupObjects.child(topicBox, ".header")

        val node = JsoupObjects(header).child(".chevron").adjacent("a").first().let { parseNode(it) }
        builder.setNode(node)

        try {
            parseTopicReplyTime(builder, JsoupObjects.child(header, ".gray").textNodes()[1].text())
        } catch (e: IllegalStateException) {
            // TODO: fix this exception and remove log code
            Crashlytics.log(JsoupObjects.child(header, ".gray").html())
            Crashlytics.logException(e)
            throw e
        }
        parseTopicTitle(builder, header)

        parseTopicContent(builder, topicBox)
        parsePostscript(builder, topicBox)
        parseTopicReplyCount(builder, parent)

        if (!builder.hasInfo()) {
            TopicListParser.parseMember(builder, JsoupObjects.child(header, ".fr"))
        }

        return if (UserState.isLoggedIn()) {
            parseFavored(builder, topicBox)
        } else {
            null
        }
    }

    internal fun parseTopicReplyTime(topicBuilder: Topic.Builder, text: String) {
        val matcher = checkNotNull(PATTERN_TOPIC_REPLY_TIME.find(text)) {
            "match reply time for topic failed: $text"
        }

        val timeStr = matcher.groupValues[1]
        topicBuilder.setReplyTime(timeStr)
    }

    private fun parseTopicTitle(builder: Topic.Builder, header: Element) {
        val title = JsoupObjects.child(header, "h1").html()

        builder.setTitle(title)
    }

    private fun parseTopicReplyCount(topicBuilder: Topic.Builder, parent: Element) {
        val gray = JsoupObjects(parent).child(".box:nth-child(3)").child(".cell").child(".gray").firstOrNull()
        if (gray == null) {
            // empty reply
            topicBuilder.setReplyCount(0)
        } else {
            val text = gray.ownText()
            val matcher = checkNotNull(PATTERN_NUMBERS.find(text))

            topicBuilder.setReplyCount(matcher.value.toInt())
        }
    }

    private fun parseComments(main: Element): List<Comment> {
        val elements = JsoupObjects(main).child(".box:nth-child(3)").child("[id^=r_]").child("table").child("tbody").child("tr")
        return elements.map { ele ->
            val avatarBuilder = Avatar.Builder()
            parseAvatar(avatarBuilder, ele)

            val td = JsoupObjects.child(ele, "td:nth-child(3)")

            val memberBuilder = Member.Builder()
            memberBuilder.setAvatar(avatarBuilder.createAvatar())
            parseMember(memberBuilder, td)

            val commentBuilder = Comment.Builder()
            commentBuilder.setMember(memberBuilder.createMember())

            parseCommentInfo(commentBuilder, td)
            parseCommentContent(commentBuilder, td)

            commentBuilder.createComment()
        }
    }

    private fun parseCommentContent(builder: Comment.Builder, ele: Element) {
        builder.setContent(JsoupObjects.child(ele, ".reply_content").html())
    }

    private fun parseCommentInfo(builder: Comment.Builder, ele: Element) {
        val tableEle = JsoupObjects.parents(ele, "div")
        // example data: r_123456
        val id = tableEle.id().substring(2).toInt()
        builder.setId(id)

        val fr = JsoupObjects.child(ele, ".fr")
        builder.setThanked(JsoupObjects(fr).child(".thank_area").any { it.hasClass("thanked") })
        builder.setFloor(JsoupObjects.child(fr, ".no").text().toInt())

        val elements = JsoupObjects(ele).child(".small").toList()

        val timeEle = elements[0]
        builder.setReplyTime(timeEle.text())

        if (elements.size == 2) {
            val matcher = checkNotNull(PATTERN_NUMBERS.find(elements[1].text()))

            val thanks = matcher.value.toInt()
            builder.setThanks(thanks)
        }
    }

    private fun parseMember(builder: Member.Builder, ele: Element) {
        builder.setUsername(JsoupObjects(ele).bfs(".dark").first().text())
    }

    private fun parseAvatar(builder: Avatar.Builder, ele: Element) {
        builder.setUrl(JsoupObjects(ele).dfs(".avatar").first().attr("src"))
    }

    private fun parseTopicContent(builder: Topic.Builder, topicBox: Element) {
        JsoupObjects(topicBox).child(".cell").child(".topic_content").firstOrNull()?.let {
            builder.setContent(it.html())
        }
    }

    private fun parsePostscript(builder: Topic.Builder, topicBox: Element) {
        val elements = JsoupObjects(topicBox).child(".subtle")
        val subtles = elements.map { ele ->
            val fade = JsoupObjects.child(ele, ".fade")
            val matcher = checkNotNull(PATTERN_POSTSCRIPT.find(fade.text()))
            val time = matcher.groupValues[1]

            val content = JsoupObjects.child(ele, ".topic_content").html()

            Postscript(content, time)
        }

        builder.setPostscripts(subtles)
    }

    @JvmStatic
    fun parseProblemInfo(html: String): String {
        val doc = Parser.toDoc(html)
        val elements = doc.select(".problem ul:first-child")
        Preconditions.checkState(elements.size == 1, "problem size isn't one")

        return elements[0].html()
    }
}
