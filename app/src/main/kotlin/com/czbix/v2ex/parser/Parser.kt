package com.czbix.v2ex.parser

import com.czbix.v2ex.BuildConfig
import com.czbix.v2ex.helper.JsoupObjects
import com.czbix.v2ex.model.Node
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class Parser {
    companion object {
        @JvmStatic
        fun toDoc(html: String): Document {
            val document = Jsoup.parse(html)
            if (!BuildConfig.DEBUG) {
                val settings = document.outputSettings().prettyPrint(false)
                document.outputSettings(settings)
            }
            return document
        }

        @JvmStatic
        fun parseOnceCode(html: String): String {
            val doc = toDoc(html)
            val ele = JsoupObjects(doc).body().child("#Wrapper").child(".content").child("#Main").child(".box").child(".cell").dfs("form").dfs("[name=once]").one

            return ele.`val`()
        }

        @JvmStatic
        fun parseNode(nodeEle: Element): Node {
            val title = nodeEle.text()
            val url = nodeEle.attr("href")
            val name = Node.getNameFromUrl(url)

            return Node.Builder().setTitle(title).setName(name).createNode()
        }
    }

    enum class PageType {
        Tab,
        Node,
        /**
         * Topic page in mobile style
         */
        Topic,
    }
}
