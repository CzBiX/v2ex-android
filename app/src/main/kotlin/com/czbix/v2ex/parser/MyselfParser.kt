package com.czbix.v2ex.parser

import com.czbix.v2ex.dao.NodeDao
import com.czbix.v2ex.helper.JsoupObjects
import com.czbix.v2ex.model.Avatar
import com.czbix.v2ex.model.LoginResult
import com.czbix.v2ex.model.Node
import com.google.common.base.Preconditions
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

object MyselfParser : Parser() {
    data class MySelfInfo(val unread: Int, val hasAward: Boolean)

    private val PATTERN_UNREAD_NUM = Pattern.compile("\\d+")

    @JvmStatic
    fun parseLoginResult(doc: Document): LoginResult {
        val tr = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Rightbar").dfs("tr").one

        val url = JsoupObjects(tr).dfs(".avatar").one.attr("src")
        val avatar = Avatar.Builder().setUrl(url).createAvatar()

        val username = JsoupObjects(tr).child("td").child(".bigger").child("a").one.text()
        return LoginResult(username, avatar)
    }

    /**
     * @return null if user signed out
     */
    @JvmStatic
    fun parseDoc(doc: Document, pageType: Parser.PageType): MySelfInfo? {
        if (pageType === Parser.PageType.Topic) {
            return parseTopic(doc)
        }

        val box = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Rightbar").child(".box").one

        val children = box.children()
        if (children.size <= 2) {
            // user signed out
            return null
        }

        val num = getNotificationsNum(box)
        val hasAward = pageType === Parser.PageType.Tab && hasAwardInTab(box)

        return MySelfInfo(num, hasAward)
    }

    // topic in mobile style
    private fun parseTopic(doc: Document): MySelfInfo? {
        val ele = JsoupObjects(doc).body().child("#Top").child(".content").dfs("tr").child("td:nth-child(3)").child("a").first()
        return if (ele.attr("href") == "/") null else MySelfInfo(0, false)
    }

    private fun hasAwardInTab(box: Element): Boolean {
        val optional = JsoupObjects(box.parent()).child(".box").child(".inner").child(".fa-gift").optional
        return optional.isPresent
    }

    @JvmStatic
    fun hasAward(html: String): Boolean {
        val doc = Parser.toDoc(html)
        val optional = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Main").child(".box").child(".cell").child(".gray").child(".fa-ok-sign").optional
        return !optional.isPresent
    }

    @JvmStatic
    fun getNotificationsNum(ele: Element): Int {
        val text = JsoupObjects(ele).child(".inner").bfs("a[href=/notifications]").one.text()
        val matcher = PATTERN_UNREAD_NUM.matcher(text)
        Preconditions.checkState(matcher.find())
        return Integer.parseInt(matcher.group())
    }

    @JvmStatic
    fun parseFavNodes(doc: Document): List<Node> {
        val elements = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Main").child(".box").child("#MyNodes").child("a")
        return elements.map { ele ->
            val name = Node.getNameFromUrl(ele.attr("href"))
            checkNotNull(NodeDao.get(name))
        }
    }
}
