package com.czbix.v2ex.ui.util

import android.text.style.URLSpan
import androidx.core.text.getSpans
import com.google.common.truth.Truth.*
import org.junit.Test

class HtmlTest {
    @Test
    fun fromHtml_test1() {
        val html = """
                start<a target="_blank" href="/i/D2MC5350.jpeg" rel="nofollow" title="在新窗口打开图片 D2MC5350.jpeg"><img src="//i.v2ex.co/D2MC5350.jpeg" class="embedded_image"></a>end
            """.trimIndent()

        val spanned = Html.fromHtml(html)
        val spans = spanned.getSpans<Any>()

        spans.forEach {
            assertThat(it).isNotInstanceOf(URLSpan::class.java)
        }
    }

    @Test
    fun fromHtml_test2() {
        val html = """
                start<a target="_blank" href="https://i.loli.net/2019/08/16/NuzGcJbt4LTVSYB.jpg" rel="nofollow">https://i.loli.net/2019/08/16/NuzGcJbt4LTVSYB.jpg</a>end
            """.trimIndent()

        val spanned = Html.fromHtml(html)

        assertThat(spanned.length).isEqualTo(9)
    }
}
