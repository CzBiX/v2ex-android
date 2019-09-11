package com.czbix.v2ex.network

import com.czbix.v2ex.db.Comment
import com.czbix.v2ex.model.*
import com.czbix.v2ex.parser.Parser
import com.czbix.v2ex.parser.TopicParser
import com.czbix.v2ex.util.TrackerUtils
import com.google.common.base.Stopwatch
import okhttp3.FormBody
import okhttp3.internal.EMPTY_REQUEST
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class V2exService @Inject constructor(
        private val helper: RequestHelper
) {

    suspend fun getTopic(topic: Topic, page: Int): TopicResponse {
        require(page > 0) {
            "Page must greater than zero"
        }

        Timber.d("Request topic with comments, topic: %s, page: %d", topic, page)

        val request = helper.newRequest(useMobile = true)
                .url(topic.url + "?p=" + page)
                .build()

        val html = helper.sendRequestSuspend(request) { response ->
            check(!response.isRedirect) {
                "Topic page shouldn't redirect"
            }
            response.body!!.string()
        }

        val doc = Parser.toDoc(html)
        helper.processUserState(doc, Parser.PageType.Topic)

        val stopwatch = Stopwatch.createStarted()
        val result = TopicParser.parseDoc(doc, topic, page)
        TrackerUtils.onParseTopic(stopwatch.elapsed(TimeUnit.MILLISECONDS),
                result.comments.size.toString())

        return result
    }

    suspend fun postComment(topic: Topic, content: String, onceToken: String) {
        Timber.d("Post comment to topic: %s", topic)

//        @Suppress("NAME_SHADOWING")
//        val onceToken = onceToken ?: helper.getOnceToken().await()

        val requestBody = FormBody.Builder()
                .add("once", onceToken)
                .add("content", content.replace("\n", "\r\n"))
                .build()

        val request = RequestHelper.newRequest(useMobile = true)
                .url(topic.url)
                .post(requestBody).build()

        helper.sendRequestSuspend(request, false) { response ->
            // v2ex will redirect if reply success
            check(response.isRedirect)
        }
    }

    suspend fun favor(favable: Favable, bool: Boolean, csrfToken: String) {
        Timber.d("Favorite %s, bool: %s", favable, bool)

        val url = if (bool) favable.getFavUrl(csrfToken) else favable.getUnFavUrl(csrfToken)
        val request = RequestHelper.newRequest().url(url)
                .build()

        helper.sendRequestSuspend(request, false) { response ->
            check(response.isRedirect)
        }
    }

    suspend fun thank(obj: Thankable, onceToken: String) {
        Timber.d("Thank %s", obj)

        val request = RequestHelper.newRequest().apply {
            url(obj.thankUrl + "?once=" + onceToken)
            post(EMPTY_REQUEST)
        }.build()

        helper.sendRequestSuspend(request)
    }

    suspend fun ignore(obj: Ignorable, onceToken: String) {
        Timber.d("Ignore %s", obj)

        val builder = RequestHelper.newRequest().url(obj.ignoreUrl + "?once=" + onceToken)
        val isComment = obj is Comment
        if (isComment) {
            builder.post(EMPTY_REQUEST)
        }
        val request = builder.build()

        helper.sendRequestSuspend(request, isComment) {}
    }
}