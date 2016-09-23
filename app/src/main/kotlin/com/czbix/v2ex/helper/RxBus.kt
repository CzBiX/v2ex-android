package com.czbix.v2ex.helper

import com.czbix.v2ex.event.BaseEvent
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.cast
import rx.subjects.Subject

object RxBus {
    private val subject: Subject<BaseEvent, BaseEvent>

    init {
        subject = PublishSubject<BaseEvent>().toSerialized()
    }

    fun post(event: BaseEvent) {
        subject.onNext(event)
    }

    fun toObservable(): Observable<BaseEvent> {
        return subject
    }

    @JvmName("subscribeAny")
    fun subscribe(scheduler: Scheduler = AndroidSchedulers.mainThread(),
                                           action: (BaseEvent) -> Unit): Subscription {
        return toObservable()
                .observeOn(scheduler)
                .subscribe(action)
    }

    inline fun <reified T : BaseEvent> subscribe(scheduler: Scheduler = AndroidSchedulers.mainThread(),
                                                                      noinline action: (T) -> Unit): Subscription {
        return toObservable()
                .filter { it is T }
                .cast<T>()
                .observeOn(scheduler)
                .subscribe(action)
    }
}
