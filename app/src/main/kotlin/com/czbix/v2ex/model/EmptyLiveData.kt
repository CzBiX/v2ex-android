package com.czbix.v2ex.model

import androidx.lifecycle.LiveData

class EmptyLiveData<T> : LiveData<T>() {
    companion object {
        fun <T> create(): EmptyLiveData<T> {
            return EmptyLiveData()
        }
    }
}
