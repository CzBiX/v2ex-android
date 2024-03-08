package com.czbix.v2ex.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import java.security.MessageDigest

object TrackerUtils {
    private lateinit var analytics: FirebaseAnalytics

    private fun hashString(data: String): String {
        val salted = "cz-%s-bix".format(data)
        return MessageDigest.getInstance("MD5")
                .digest(salted.toByteArray())
                .fold("") { str, it ->
                        str + "%02x".format(it)
                }
    }

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    fun setUserId(id: String?) {
        val hashedId = if (id == null) null else hashString(id)
        analytics.setUserId(hashedId)
    }

    fun onTopicSwitchReply(isShow: Boolean) {
        val params = Bundle().apply {
            putBoolean("is_show", isShow)
        }

        analytics.logEvent(Event.SWITCH_REPLY, params)
    }

    fun onTopicReply() {
        analytics.logEvent(Event.REPLY_TOPIC, null)
    }

    fun onSearch() {
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH, null)
    }

    fun onParseTopic(time: Long, commentCount: Int) {
        val params = Bundle().apply {
            putInt("cost_time", time.toInt())
            putInt("topic_count", commentCount)
        }
        analytics.logEvent(Event.PARSE_TOPIC, params)
    }

    private object Event {
        const val SWITCH_REPLY = "switch_reply"
        const val REPLY_TOPIC = "reply_topic"
        const val PARSE_TOPIC = "parse_topic"
    }
}