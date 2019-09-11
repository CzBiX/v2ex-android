package com.czbix.v2ex.model

import androidx.lifecycle.LiveData

/**
 * A LiveData class that has `null` value.
 */
class AbsentLiveData<T : Any?>(
        data: T? = null
): LiveData<T>() {
    init {
        // use post instead of set since this can be created on any thread
        postValue(data)
    }
}