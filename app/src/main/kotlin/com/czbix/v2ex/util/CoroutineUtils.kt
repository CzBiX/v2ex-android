package com.czbix.v2ex.util

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


suspend fun <E> ReceiveChannel<E>.debounce(
        wait: Long,
        scope: CoroutineScope = GlobalScope,
        context: CoroutineContext = EmptyCoroutineContext
): ReceiveChannel<E> = scope.produce(context) {
    var lastTimeout: Job? = null
    consumeEach {
        lastTimeout?.cancel()
        lastTimeout = launch {
            delay(wait)
            send(it)
        }
    }
    lastTimeout?.join()
}

fun <E> ReceiveChannel<E>.throttle(
        wait: Long,
        scope: CoroutineScope = GlobalScope,
        context: CoroutineContext = EmptyCoroutineContext
): ReceiveChannel<E> = scope.produce(context) {
    var nextTime = 0L
    consumeEach {
        val curTime = System.currentTimeMillis()
        if (curTime >= nextTime) {
            nextTime = curTime + wait
            send(it)
        }
    }
}
