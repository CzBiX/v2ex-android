package com.czbix.v2ex.ui.loader

import android.content.Context
import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.dao.TopicDao
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.model.TopicWithComments
import com.czbix.v2ex.network.RequestHelper
import java.util.*

class TopicLoader(context: Context, private val mTopic: Topic) : AsyncTaskLoader<TopicWithComments>(context) {
    private var mPage: Int = 0

    init {
        mPage = 1
    }

    fun setPage(page: Int) {
        if (page == mPage) {
            return
        }

        mPage = page
        mResult = null
        onContentChanged()
    }

    @Throws(ConnectionException::class, RemoteException::class)
    override fun loadInBackgroundWithException(): TopicWithComments {
        val topicWithComments = RequestHelper.getTopicWithComments(mTopic, mPage)
        if (mPage == 1 && topicWithComments.mComments.size > 0) {
            val lastRead = TopicDao.getLastReadReply(mTopic.id)
            if (lastRead > 7) {
                val commentIds = topicWithComments.mComments.map { it.floor }
                val index = Collections.binarySearch(commentIds, lastRead)
                // the closest and latest comment of last read pos
                topicWithComments.mLastReadPos = if (index >= 0) index else -index - 2
            }
        }

        return topicWithComments
    }
}
