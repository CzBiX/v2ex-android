package com.czbix.v2ex.parser

import android.text.Spanned
import android.text.style.ImageSpan
import androidx.core.text.getSpans
import com.czbix.v2ex.BuildConfig
import com.czbix.v2ex.db.Member
import com.czbix.v2ex.helper.JsoupObjects
import com.czbix.v2ex.model.Avatar
import com.czbix.v2ex.model.ContentBlock
import com.czbix.v2ex.model.Node
import com.czbix.v2ex.util.MiscUtils
import com.czbix.v2ex.util.ViewUtils
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
            memberBuilder.username = Member.getNameFromUrl(url)

            // get member avatar
            ele = ele.child(0)
            val avatarBuilder = Avatar.Builder()
            check(ele.tagName() == "img")
            avatarBuilder.setUrl(ele.attr("src"))

            memberBuilder.avatar = avatarBuilder.build()

            return memberBuilder.build()
        }

        fun parseHtml2Blocks(html: String): List<ContentBlock> {
            val builder = ViewUtils.parseHtml(html, null, true)

            if (builder !is Spanned) {
                val block = ContentBlock.TextBlock(0, builder)

                return listOf(block)
            }

            val spans = builder.getSpans<Any>()

            var lastEndPos = 0
            var index = 0
            val blocks = mutableListOf<ContentBlock>()
            for (span in spans) {
                if (span !is ImageSpan && span !is PreKind) {
                    continue
                }

                val start = builder.getSpanStart(span)
                val text = builder.subSequence(lastEndPos, start).trim()
                if (text.isNotEmpty()) {
                    blocks.add(ContentBlock.TextBlock(index++, text))
                }

                val end by lazy {
                    builder.getSpanEnd(span)
                }

                if (span is ImageSpan) {
                    val url = MiscUtils.formatUrl(span.source!!)
                    blocks.add(ContentBlock.ImageBlock(index++, url))
                } else {
                    val preText = builder.subSequence(start, end - 1)
                    blocks.add(ContentBlock.PreBlock(index++, preText))
                }

                lastEndPos = end
            }

            val length = builder.length
            if (lastEndPos != length) {
                val text = builder.subSequence(lastEndPos, length).trim()

                blocks.add(ContentBlock.TextBlock(index, text))
            }

            return blocks
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

    class PreKind

    data class SignInFormData(val username: String, val password: String,
                              val once: String, val captcha: String)

}
