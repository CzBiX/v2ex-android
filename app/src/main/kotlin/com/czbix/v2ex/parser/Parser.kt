package com.czbix.v2ex.parser

import com.czbix.v2ex.BuildConfig
import com.czbix.v2ex.helper.JsoupObjects
import com.czbix.v2ex.model.Avatar
import com.czbix.v2ex.model.Member
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
            val ele = JsoupObjects(doc).body().child("#Wrapper").child(".content").child(".box")
                    .child(".cell").dfs("form").dfs("input[name=once]").first()

            return ele.`val`()
        }

        @JvmStatic
        fun parseSignInForm(html: String): SignInFormData {
            val doc = toDoc(html)
            val form = JsoupObjects(doc).body().child("#Wrapper").child(".content").child(".box")
                    .child(".cell").dfs("form").first()
            val name = JsoupObjects(form).dfs("input[type=text]").first()
            val once = JsoupObjects(form).dfs("input[name=once]").first()
            val password = once.nextElementSibling()
            val captcha = JsoupObjects(form).dfs("input.sl").last()

            check(password.tagName() == "input")

            return SignInFormData(
                    name.attr("name"), password.attr("name"), once.`val`(), captcha.attr("name")
            )
        }

        @JvmStatic
        fun parseNode(nodeEle: Element): Node {
            val title = nodeEle.text()
            val url = nodeEle.attr("href")
            val name = Node.getNameFromUrl(url)

            return Node.Builder().setTitle(title).setName(name).createNode()
        }

        @JvmStatic
        protected fun parseMember(td: Element): Member {
            val memberBuilder = Member.Builder()

            var ele = td.child(0)
            // get member url
            check(ele.tagName() == "a")
            val url = ele.attr("href")
            memberBuilder.setUsername(Member.getNameFromUrl(url))

            // get member avatar
            ele = ele.child(0)
            val avatarBuilder = Avatar.Builder()
            check(ele.tagName() == "img")
            avatarBuilder.setUrl(ele.attr("src"))

            memberBuilder.setAvatar(avatarBuilder.createAvatar())

            return memberBuilder.createMember()
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

    data class SignInFormData(val username: String, val password: String,
                              val once: String, val captcha: String)

}
