package com.czbix.v2ex.ui.model

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.db.Comment
import com.czbix.v2ex.db.TopicRecord
import com.czbix.v2ex.db.TopicRecordDao
import com.czbix.v2ex.model.*
import com.czbix.v2ex.network.repository.TopicRepository
import com.czbix.v2ex.util.liveData
import com.czbix.v2ex.util.then
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

class TopicViewModel @Inject constructor(
        private val topicRepo: TopicRepository,
        private val dao: TopicRecordDao,
        private val prefStore: PrefStore
) : ViewModel() {
    private val pageToLoad = MutableLiveData<Int>()
    private val baseTopic = MutableLiveData<Topic>()
    private val userAction = MutableLiveData<Action>()

    private var maxPage = 1
    private var onceToken: String? = null
    private var csrfToken: String? = null
    private var fetchedPage = BitSet()

    val favored = MediatorLiveData<Boolean>()
    lateinit var lastTopic: Topic
        private set

    val result = pageToLoad.switchMap { page ->
        topicRepo.loadTopic(baseTopic.value!!, page)
    }
    val comments = result.then {
        baseTopic.switchMap {
            topicRepo.loadLocalTopicComments(it.id)
        }
    }

    val lastReadRecord = result.then {
        baseTopic.switchMap {
            liveData {
                dao.getRecordById(it.id)
            }
        }
    }

    val userActionResult = userAction.switchMap {
        handleUserAction(it)
    }

    private fun handleUserAction(action: Action): LiveData<Pair<Action, Resource<Unit>>> {
        val data = when (action) {
            is Action.PostComment -> topicRepo.postComment(lastTopic, action.content, onceToken!!)
            is Action.FavTopic -> topicRepo.favTopic(lastTopic, action.bool, csrfToken!!)
            is Action.Thank -> topicRepo.thank(action.target, onceToken!!)
            is Action.Ignore -> topicRepo.ignore(action.target, onceToken!!)
        }

        return data.map {
            action to it
        }
    }

    private val resultObserver = Observer<Resource<TopicResponse>> {
        processData(it)
    }
    private val userActionResultObserver = Observer<Pair<Action, Resource<Unit>>> { (action, resource) ->
        if (action !is Action.FavTopic || resource.status != Resource.Status.SUCCESS) {
            return@Observer
        }

        favored.value = action.bool
    }

    init {
        result.observeForever(resultObserver)
        userActionResult.observeForever(userActionResultObserver)

        favored.addSource(result) {
            if (it is Resource.Success) {
                favored.value = it.data.topic.isFavored
            }
        }
    }

    fun setTopic(topic: Topic) {
        baseTopic.value = topic
        lastTopic = topic

        pageToLoad.value = 1
    }

    fun doAction(action: Action): Job? {
        if (prefStore.isUndoEnabled) {
            return viewModelScope.launch {
                delay(3000)
                userAction.postValue(action)
            }
        }

        userAction.value = action
        return null
    }

    override fun onCleared() {
        super.onCleared()

        result.removeObserver(resultObserver)
        userActionResult.removeObserver(userActionResultObserver)
    }

    fun refresh(page: Int = NO_PAGE) {
        val pageToRefresh = if (page == NO_PAGE) pageToLoad.value else page

        pageToLoad.value = pageToRefresh
    }

    fun fetchPageIfNeed(page: Int) {
        if (page !in 2 .. maxPage || fetchedPage.get(page - 1) || pageToLoad.value == page) {
            return
        }

        pageToLoad.value = page
    }

    private fun processData(result: Resource<TopicResponse>) {
        if (result !is Resource.Success) {
            return
        }

        val data = result.data
        fetchedPage.set(data.curPage - 1)
        lastTopic = data.topic
        maxPage = data.maxPage
        onceToken = data.onceToken
        csrfToken = data.csrfToken
    }

    fun markReadPosition() {
        val position = lastTopic.replyCount
        val newRecord by lazy {
            TopicRecord(lastTopic.id, lastTopic.title!!, 0, 0)
        }
        val record = lastReadRecord.value ?: newRecord

        GlobalScope.launch(Dispatchers.IO) {
            record.lastReadAt = System.currentTimeMillis()
            record.lastReadComment = position

            dao.updateRecord(record)
        }
    }

    sealed class Action(
            val itemPage: Int
    ) {
        class PostComment(val content: String) : Action(NO_PAGE)

        abstract class Thank(open val target: Thankable, itemPage: Int) : Action(itemPage)
        class ThankTopic(override val target: Topic) : Thank(target, NO_PAGE)
        class ThankComment(override val target: Comment) : Thank(target, target.page)

        abstract class Ignore(open val target: Ignorable, itemPage: Int) : Action(itemPage)
        class IgnoreTopic(override val target: Topic) : Ignore(target, NO_PAGE)
        class IgnoreComment(override val target: Comment) : Ignore(target, target.page)

        class FavTopic(val bool: Boolean) : Action(NO_PAGE)
    }

    companion object {
        const val NO_PAGE = 0
    }
}