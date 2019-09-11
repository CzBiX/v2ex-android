package com.czbix.v2ex.ui.loader

import android.content.Context
import com.czbix.v2ex.db.TopicRecordDao
import com.czbix.v2ex.model.Page
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.network.RequestHelper
import kotlinx.coroutines.runBlocking

class TopicListLoader(
        context: Context,
        private val mPage: Page,
        private val dao: TopicRecordDao
) : AsyncTaskLoader<TopicListLoader.TopicList>(context) {
    @Throws(Exception::class)
    override fun loadInBackgroundWithException(): TopicList {
        val topics = runBlocking {
            RequestHelper.getTopics(mPage)
        }

        for (topic in topics) {
            val lastRead = dao.getLastReadComment(topic.id) ?: 0
            if (lastRead >= topic.replyCount) {
                topic.hasRead = true
            }
        }
        return topics
    }

    class TopicList(list: List<Topic>, val isFavorited: Boolean, val onceToken: String? = null): List<Topic> by list
}
