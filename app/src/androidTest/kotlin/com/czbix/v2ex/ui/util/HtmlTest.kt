package com.czbix.v2ex.ui.util

import android.text.style.URLSpan
import androidx.core.text.getSpans
import io.kotlintest.inspectors.forAll
import io.kotlintest.matchers.types.shouldNotBeSameInstanceAs
import io.kotlintest.shouldBe
import org.junit.Test

class HtmlTest {
    @Test
    fun fromHtml_test1() {
        val html = """
                start<a target="_blank" href="/i/D2MC5350.jpeg" rel="nofollow" title="在新窗口打开图片 D2MC5350.jpeg"><img src="//i.v2ex.co/D2MC5350.jpeg" class="embedded_image"></a>end
            """.trimIndent()

        val spanned = Html.fromHtml(html)
        val spans = spanned.getSpans<Any>()

        spans.forAll {
            it shouldNotBeSameInstanceAs URLSpan::class
        }
    }

    @Test
    fun fromHtml_test2() {
        val html = """
                start<a target="_blank" href="https://i.loli.net/2019/08/16/NuzGcJbt4LTVSYB.jpg" rel="nofollow">https://i.loli.net/2019/08/16/NuzGcJbt4LTVSYB.jpg</a>end
            """.trimIndent()

        val spanned = Html.fromHtml(html)

        spanned.length shouldBe 9
    }

    @Test
    fun fromHtml_withCfEncoded() {
        val html = """
                支持 <a href="/cdn-cgi/l/email-protection" class="__cf_email__" data-cfemail="4f787d7f3f0f7879777f293f3c">[email&nbsp;protected]</a>，<a href="/cdn-cgi/l/email-protection" class="__cf_email__" data-cfemail="80b1b0b8b0f0c0b9b6b0e6f0f3">[email&nbsp;protected]</a> 超级慢动作视频
                """.trimIndent()

        val spanned = Html.fromHtml(html)

        spanned.toString() shouldBe "支持 720p@7680fps，1080p@960fps 超级慢动作视频"
    }
}
