package com.czbix.v2ex.util

import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.observable
import rx.schedulers.Schedulers

inline fun <T> async(scheduler: Scheduler = Schedulers.io(), crossinline runnable: () -> T): Observable<T> {
    return observable<T> { subscriber ->
        subscriber.onNext(runnable())
        subscriber.onCompleted()
    }.subscribeOn(scheduler)
}

inline fun <T, R> Observable<T>.amap(scheduler: Scheduler = Schedulers.computation(),
                                     crossinline block: (T) -> R): Observable<R> {
    return this.subscribeOn(scheduler).map { block(it) }
}

inline fun <T, R> Observable<T>.aflatMap(scheduler: Scheduler = Schedulers.computation(),
                                     crossinline block: (T) -> Observable<R>): Observable<R> {
    return this.subscribeOn(scheduler).flatMap { block(it) }
}

inline fun <T> Observable<T>.aforeach(scheduler: Scheduler = Schedulers.computation(),
                                      crossinline callable: (T) -> Unit): Observable<T> {
    return this.subscribeOn(scheduler).doOnNext{ callable(it) }
}

inline fun <T> Observable<T>.await(scheduler: Scheduler = AndroidSchedulers.mainThread(),
                                   crossinline callable: (T) -> Unit): Subscription {
    return this.observeOn(scheduler).subscribe { callable(it) }
}

inline fun <T> Observable<T>.await(scheduler: Scheduler = AndroidSchedulers.mainThread(),
                                   crossinline onNext: (T) -> Unit, crossinline onError: (Throwable) -> Unit): Subscription {
    return this.observeOn(scheduler).subscribe({ onNext(it) }, { onError(it) })
}
