package com.czbix.v2ex.util

import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

inline fun <T> async(scheduler: Scheduler = Schedulers.computation(), crossinline runnable: () -> T): Observable<T> {
    return Observable.create<T> { subscriber ->
        subscriber.onNext(runnable())
        subscriber.onCompleted()
    }.subscribeOn(scheduler)
}

fun <T : Any> Observable<T>.await(callable: (T) -> Unit): Subscription {
    return this.observeOn(AndroidSchedulers.mainThread()).subscribe(callable)
}

fun <T> Observable<T>.await(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Subscription {
    return this.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError)
}

fun <T> Observable<T>.await(observer: Observer<T>): Subscription {
    return this.observeOn(AndroidSchedulers.mainThread()).subscribe(observer)
}

fun MutableList<Subscription>.unsubscribe() {
    this.forEach { it.unsubscribe() }
    this.clear()
}

/**
 * @see rx.exceptions.Exceptions.propagate
 */
fun <T> Observable<T>.result(): T {
    try {
        return this.toBlocking().last()
    } catch (e: RuntimeException) {
        // unwarp RuntimeException for Exceptions.propagate
        val cause = e.cause
        if (cause == null) {
            throw e
        } else if (cause === e) {
            throw e
        } else {
            throw cause
        }
    }
}
