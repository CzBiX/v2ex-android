package com.czbix.v2ex.util

import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.observable
import rx.schedulers.Schedulers

inline fun <T> async(scheduler: Scheduler = Schedulers.computation(), crossinline runnable: () -> T): Observable<T> {
    return observable<T> { subscriber ->
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

fun <T> Observable<T>.await(onNext: (T) -> Unit, onError: (Throwable) -> Unit, onComplete: () -> Unit): Subscription {
    return this.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError, onComplete)
}

fun MutableList<Subscription>.unsubscribe() {
    this.forEach { it.unsubscribe() }
    this.clear()
}

fun <T> Observable<T>.result(): T {
    return this.toBlocking().last()
}
