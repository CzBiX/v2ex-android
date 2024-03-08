package com.czbix.v2ex.model

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class NetworkBoundResource<ResultType, RequestType>
@MainThread constructor() {
    private val result = MediatorLiveData<Resource<ResultType>>()
    private var started = false

    init {
        result.value = Resource.Loading()
    }

    fun start() {
        if (started) {
            return
        }

        started = true

        val dbSource = loadFromDb()
        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource)
            } else {
                result.addSource(dbSource) { newData ->
                    setValue(Resource.Success(newData))
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val response = createCall()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource) { newData ->
            setValue(Resource.Loading(newData))
        }
        result.addSource(response) { resource ->
            result.removeSource(response)
            result.removeSource(dbSource)
            when (resource) {
                is Resource.Success -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        saveCallResult(processResponse(resource))

                        withContext(Dispatchers.Main) {
                            // we specially request a new live data,
                            // otherwise we will get immediately last cached value,
                            // which may not be updated with latest results received from network.
                            result.addSource(loadFromDb()) { newData ->
                                setValue(Resource.Success(newData))
                            }
                        }
                    }
                }
                /*
                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        // reload from disk whatever we had
                        result.addSource(loadFromDb()) { newData ->
                            setValue(Resource.success(newData))
                        }
                    }
                }
                 */
                is Resource.Failed -> {
                    onFetchFailed()
                    result.addSource(dbSource) { newData ->
                        setValue(Resource.Failed(resource.exception, newData))
                    }
                }
                else -> error("Unknown network resource status")
            }
        }
    }

    protected open fun onFetchFailed() {}

    fun asLiveData(): LiveData<Resource<ResultType>> {
        start()

        return result
    }

    @WorkerThread
    protected open fun processResponse(response: Resource.Success<RequestType>) = response.data

    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): LiveData<Resource<RequestType>>

    class NetworkOnlyResource<T : Any>(private val call: () -> LiveData<Resource<T>>) : NetworkBoundResource<T, T>() {
        private val result = MutableLiveData<T>(null)

        override fun saveCallResult(item: T) {
            result.postValue(item)
        }

        override fun shouldFetch(data: T?): Boolean {
            return true
        }

        override fun loadFromDb(): LiveData<T> {
            return result
        }

        override fun createCall(): LiveData<Resource<T>> {
            return call()
        }
    }
}