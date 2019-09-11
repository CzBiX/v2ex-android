package com.czbix.v2ex.network.repository

import androidx.lifecycle.LiveData
import com.czbix.v2ex.model.NetworkBoundResource
import com.czbix.v2ex.model.Resource
import com.czbix.v2ex.util.liveData

open class BaseRepository {
    protected fun <T> wrapCall(call: suspend () -> T): LiveData<Resource<T>> {
        return liveData {
            try {
                val result = call()
                Resource.Success(result)
            } catch (e: Exception) {
                Resource.Failed<T>(e)
            }
        }
    }

    protected inline fun <T : Any> networkOnlyCall(crossinline call: suspend () -> T): LiveData<Resource<T>> {
        return NetworkBoundResource.NetworkOnlyResource {
            wrapCall {
                call()
            }
        }.asLiveData()
    }
}