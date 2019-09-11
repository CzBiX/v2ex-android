package com.czbix.v2ex.model

sealed class Resource<out T>(
        val status: Status,
        open val data: T?,
        open val exception: Exception?
) {
    enum class Status {
        LOADING,
        SUCCESS,
        FAILED,
    }

    fun <R> map(block: (T) -> R): Resource<R> {
        return when (this) {
            is Loading -> Loading(data?.let(block))
            is Success -> Success(data.let(block))
            is Failed -> Failed(exception, data?.let(block))
        }
    }

    data class Loading<out T>(
            override val data: T? = null
    ) : Resource<T>(Status.LOADING, data, null)

    data class Success<out T>(
            override val data: T
    ) : Resource<T>(Status.SUCCESS, data, null)

    data class Failed<out T>(
            override val exception: Exception,
            override val data: T? = null
    ) : Resource<T>(Status.FAILED, data, exception)

    override fun toString(): String {
        return when (this) {
            is Success -> "Success[data=$data]"
            is Failed -> "Failed[exception=$exception]"
            is Loading -> "Loading"
        }
    }
}