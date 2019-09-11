package com.czbix.v2ex.network.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.czbix.v2ex.db.CommentAndMember
import com.czbix.v2ex.db.CommentDao
import com.czbix.v2ex.model.*
import com.czbix.v2ex.network.V2exService
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepository @Inject constructor(
        private val service: V2exService,
        private val commentDao: CommentDao
) : BaseRepository() {

    fun loadTopic(topic: Topic, page: Int): LiveData<Resource<TopicResponse>> {
        return object : NetworkBoundResource<TopicResponse, TopicResponse>() {
            private val liveData = MutableLiveData<TopicResponse>(null)

            override fun saveCallResult(item: TopicResponse) {
                runBlocking {
                    commentDao.updateCommentAndMembers(item.comments, CommentDao.CommentPage(topic.id, page))
                }
                liveData.postValue(item)
            }

            override fun shouldFetch(data: TopicResponse?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<TopicResponse> {
                return liveData
            }

            override fun createCall(): LiveData<Resource<TopicResponse>> {
                return wrapCall {
                    service.getTopic(topic, page)
                }
            }
        }.asLiveData()
    }

    fun loadLocalTopicComments(topicId: Int): LiveData<PagedList<CommentAndMember>> {
        return commentDao.getCommentsByTopicId(topicId).toLiveData(pageSize = 100)
    }

    fun postComment(topic: Topic, content: String, onceToken: String) = networkOnlyCall {
        service.postComment(topic, content, onceToken)
    }

    fun favTopic(topic: Topic, bool: Boolean, csrfToken: String) = networkOnlyCall {
        service.favor(topic, bool, csrfToken)
    }

    fun thank(thankable: Thankable, onceToken: String) = networkOnlyCall {
        service.thank(thankable, onceToken)
    }

    fun ignore(ignorable: Ignorable, onceToken: String) = networkOnlyCall {
        service.ignore(ignorable, onceToken)
    }
}