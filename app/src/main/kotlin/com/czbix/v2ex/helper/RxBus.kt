package com.czbix.v2ex.helper

import com.czbix.v2ex.event.BaseEvent
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

object RxBus {
    private val subject: Subject<BaseEvent>

    init {
        subject = PublishSubject.create<BaseEvent>().toSerialized()
    }

    fun post(event: BaseEvent) {
        subject.onNext(event)
    }

    @JvmName("toObservableAny")
    fun toObservable(): Observable<BaseEvent> {
        return subject
    }

    inline fun <reified T : BaseEvent> toObservable(): Observable<T> {
        return toObservable().filter { it is T }.cast()
    }

    @JvmName("subscribeAny")
    fun subscribe(scheduler: Scheduler = AndroidSchedulers.mainThread(),
                  action: (BaseEvent) -> Unit): Disposable {
        return toObservable()
                .observeOn(scheduler)
                .subscribe(action)
    }

    inline fun <reified T : BaseEvent> subscribe(scheduler: Scheduler = AndroidSchedulers.mainThread(),
                                                 noinline action: (T) -> Unit): Disposable {
        return toObservable<T>()
                .observeOn(scheduler)
                .subscribe(action)
    }
}
