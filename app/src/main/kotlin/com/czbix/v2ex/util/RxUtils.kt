package com.czbix.v2ex.util

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.observers.ConsumerSingleObserver

fun <T : Any> Single<T>.await(onSuccess: (T) -> Unit): Disposable {
    return this.observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess)
}

fun <T> Single<T>.await(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {
    return this.observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess, onError)
}

fun MutableList<Disposable>.dispose() {
    this.forEach { it.dispose() }
}

/**
 * @see io.reactivex.exceptions.Exceptions.propagate
 */
fun <T> Single<T>.result(): T {
    try {
        return this.blockingGet()
    } catch (e: RuntimeException) {
        // unwarp RuntimeException for Exceptions.propagate
        val cause = e.cause
        when {
            cause == null -> throw e
            cause === e -> throw e
            else -> throw cause
        }
    }
}
