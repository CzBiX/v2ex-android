package com.czbix.v2ex.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.czbix.v2ex.model.EmptyLiveData
import com.czbix.v2ex.model.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun <T> liveData(
        scope: CoroutineScope = GlobalScope,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend () -> T
): LiveData<T> {
    val liveData = MutableLiveData<T>()
    scope.launch(context) {
        val data = block()
        liveData.postValue(data)
    }

    return liveData
}

fun <T> emptyLiveData() = EmptyLiveData.create<T>()

fun <T, R> LiveData<T>.then(condition: ((T) -> Boolean)? = null, block: () -> LiveData<R>): LiveData<R> {
    var conditionMeet = false
    var valueEmitted = false

    val result = MediatorLiveData<R>()
    val liveData = block()

    val emit = { it: R? ->
        if (conditionMeet && valueEmitted) {
            result.value = it ?: liveData.value
        }
    }

    result.addSource(this) {
        if (condition == null || condition(it)) {
            result.removeSource(this)

            conditionMeet = true
            emit(null)
        }
    }
    result.addSource(liveData) {
        if (!valueEmitted) {
            valueEmitted = true
        }
        emit(it)
    }

    return result
}

fun <T, R> LiveData<Resource<T>>.then(block: () -> LiveData<R>): LiveData<R> {
    return this.then({ it.status == Resource.Status.SUCCESS}, block)
}
