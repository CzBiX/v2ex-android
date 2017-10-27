package com.czbix.v2ex.ui.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PageKeyedDataSource
import android.arch.paging.PagedList
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.czbix.v2ex.dao.NodeDao
import com.czbix.v2ex.dao.TopicDao
import com.czbix.v2ex.model.Node
import com.czbix.v2ex.model.Page
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.parser.TopicListParser
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.dispose
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await
import java.io.IOException

class TopicListViewModel(
        page: Page
) : ViewModel() {
    val page: Page
    val topics: LiveData<PagedList<Topic>>
    val networkState: LiveData<NetworkState>
    val refreshState: LiveData<NetworkState>

    private val mFavorite: MutableLiveData<Boolean> = MutableLiveData()
    private val mOnceToken: MutableLiveData<String> = MutableLiveData()

    private val sourceFactory: Factory
    private val mDisposables: MutableList<Disposable> = mutableListOf()

    val favorite: LiveData<Boolean> = mFavorite
    val onceToken: LiveData<String> = mOnceToken

    init {
        this.page = if (page is Node && !page.hasInfo()) {
            NodeDao.get(page.name) ?: page
        } else {
            page
        }

        sourceFactory = Factory(page)
        topics = LivePagedListBuilder(sourceFactory, 20).build()

        networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.networkState
        }
        refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
    }

    fun toggleFavorite(value: Boolean) {
        mFavorite.postValue(value)
    }

    val refresh = {
        sourceFactory.sourceLiveData.value?.invalidate()
    }

    val retry = {
        sourceFactory.sourceLiveData.value?.retryAllFailed()
    }

    override fun onCleared() {
        mDisposables.dispose()
    }

    class TopicListDataSource(private val page: Page): PageKeyedDataSource<Int, Topic>() {
        private val tag = javaClass.simpleName
        private var retry: (() -> Any)? = null
        val networkState = MutableLiveData<NetworkState>()
        val initialLoad = MutableLiveData<NetworkState>()

        fun retryAllFailed() {
            val prevRetry = retry
            retry = null
            prevRetry?.let {
                launch {
                    it.invoke()
                }
            }
        }

        private fun loadWithReadState(topicList: TopicListParser.TopicList) {
            // TODO:
            for (topic in topicList) {
                val lastRead = TopicDao.getLastReadReply(topic.id)
                if (lastRead >= topic.replyCount) {
                    topic.setHasRead()
                }
            }
        }

        private fun logLoading(pageNum: Int) {
            val log = "Load list: ${page.title}, pageNum: $pageNum."
            Crashlytics.log(log)
            LogUtils.d(tag, log)
        }

        override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Topic>) {
            logLoading(1)
            networkState.postValue(NetworkState.LOADING)
            initialLoad.postValue(NetworkState.LOADING)

            launch {
                try {
                    val topics = RequestHelper.getTopics(page).await()
                    loadWithReadState(topics)

                    retry = null

                    networkState.postValue(NetworkState.LOADED)
                    initialLoad.postValue(NetworkState.LOADED)

                    val hasNextPage = topics.maxPage > 1
                    callback.onResult(topics, 1, if (hasNextPage) 2 else null)
                } catch (e: IOException) {
                    Log.d(tag, "Topic list initial load failed.", e)
                    retry = {
                        loadInitial(params, callback)
                    }
                    val error = NetworkState.error(e.message ?: "Unknown error")

                    networkState.postValue(error)
                    initialLoad.postValue(error)
                }
            }
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Topic>) {
            logLoading(params.key)
            networkState.postValue(NetworkState.LOADING)

            launch {
                try {
                    val topics = RequestHelper.getTopics(page, params.key).await()
                    loadWithReadState(topics)

                    retry = null

                    val hasNextPage = topics.maxPage > params.key
                    callback.onResult(topics, if(hasNextPage) params.key + 1 else null)

                    networkState.postValue(NetworkState.LOADED)
                } catch (e: IOException) {
                    Log.d(tag, "Topic list load after failed.", e)

                    retry = {
                        loadAfter(params, callback)
                    }

                    val error = NetworkState.error(e.message ?: "Unknown error")

                    networkState.postValue(error)
                }
            }
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Topic>) {
        }
    }

    class Factory(private val page: Page) : DataSource.Factory<Int, Topic>() {
        val sourceLiveData = MutableLiveData<TopicListDataSource>()

        override fun create(): DataSource<Int, Topic> {
            val source = TopicListDataSource(page)
            sourceLiveData.postValue(source)
            return source
        }
    }
}
