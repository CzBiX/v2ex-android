package com.czbix.v2ex.ui.loader

import android.content.Context

import com.czbix.v2ex.dao.TopicDao
import com.czbix.v2ex.model.Page
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.network.RequestHelper

class TopicListLoader(context: Context, private val mPage: Page) : AsyncTaskLoader<TopicListLoader.TopicList>(context) {
    @Throws(Exception::class)
    override fun loadInBackgroundWithException(): TopicList {
        val topics = RequestHelper.getTopics(mPage)
        for (topic in topics) {
            val lastRead = TopicDao.getLastReadReply(topic.id)
            if (lastRead >= topic.replyCount) {
                topic.hasRead = true
            }
        }
        return topics
    }

    class TopicList(list: List<Topic>, val isFavorited: Boolean, val onceToken: String? = null): List<Topic> by list
}
