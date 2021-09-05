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
        val tr = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Rightbar").dfs("tr").first()

        val url = JsoupObjects(tr).dfs(".avatar").first().attr("src")
        val avatar = Avatar.Builder().setUrl(url).build()

        val username = JsoupObjects(tr).child("td").child(".bigger").child("a").first().text()
        return LoginResult(username, avatar)
    }

    /**
     * @return null if user signed out
     */
    @JvmStatic
    fun parseDoc(doc: Document, pageType: PageType): MySelfInfo? {
        if (pageType === PageType.Topic) {
            return parseTopic(doc)
        }

        val box = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Rightbar").child(".box").first()

        val children = box.children()
        if (children.size <= 2) {
            // user signed out
            return null
        }

        val num = getNotificationsNum(box)
        val hasAward = pageType === PageType.Tab && hasAwardInTab(box)

        return MySelfInfo(num, hasAward)
    }

    // topic in mobile style
    private fun parseTopic(doc: Document): MySelfInfo? {
        val ele = JsoupObjects(doc).body().child("header", "#site-header-menu").bfs("a[href=/settings]").firstOrNull()
        return if (ele == null) null else MySelfInfo(0, false)
    }

    private fun hasAwardInTab(box: Element): Boolean {
        val ele = JsoupObjects(box.parent()).child(".box").child(".inner").child(".fa-gift").firstOrNull()
        return ele != null
    }

    @JvmStatic
    fun hasAward(html: String): Boolean {
        val doc = toDoc(html)
        val ele = JsoupObjects(doc).body().child("#Wrapper").child(".content").child(".box")
                .child(".cell").child(".gray").child(".fa-ok-sign").firstOrNull()
        return ele == null
    }

    @JvmStatic
    fun getNotificationsNum(ele: Element): Int {
        val text = JsoupObjects(ele).child(".cell").bfs("a[href=/notifications]").first().text()
        val matcher = PATTERN_UNREAD_NUM.matcher(text)
        Preconditions.checkState(matcher.find())
        return Integer.parseInt(matcher.group())
    }

    @JvmStatic
    fun parseFavNodes(doc: Document): List<Node> {
        val elements = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Main").child(".box").child("#my-nodes").child("a")
        return elements.map { ele ->
            val name = Node.getNameFromUrl(ele.attr("href"))
            checkNotNull(NodeDao.get(name))
        }
    }
}
