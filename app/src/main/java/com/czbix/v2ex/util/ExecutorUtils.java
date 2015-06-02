package com.czbix.v2ex.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {
    private static final ExecutorService CACHED_POOL = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService SCHEDULED_POOL = Executors.newScheduledThreadPool(0);

    /**
     * @see ExecutorService#submit(Callable)
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return CACHED_POOL.submit(task);
    }

    /**
     * @see ExecutorService#submit(Runnable)
     */
    public static Future<?> submit(Runnable task) {
        return CACHED_POOL.submit(task);
    }

    /**
     * @see ExecutorService#submit(Runnable, Object)
     */
    public static <T> Future<T> submit(Runnable task, T result) {
        return CACHED_POOL.submit(task, result);
    }

    /**
     * @see ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
     */
    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return SCHEDULED_POOL.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    /**
     * @see ScheduledExecutorService#schedule(Runnable, long, TimeUnit)
     */
    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return SCHEDULED_POOL.schedule(command, delay, unit);
    }

    /**
     * @see ScheduledExecutorService#schedule(Callable, long, TimeUnit)
     */
    public static <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return SCHEDULED_POOL.schedule(callable, delay, unit);
    }

    /**
     * @see ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return SCHEDULED_POOL.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /**
     * @see ExecutorService#execute(Runnable)
     */
    public static void execute(Runnable command) {
        CACHED_POOL.execute(command);
    }
}
