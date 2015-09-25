package com.czbix.v2ex.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {
    private static final Handler mUiHandler;
    private static final ExecutorService mCachePool;
    private static final ScheduledExecutorService mScheduledPool;

    static {
        mUiHandler = new Handler(Looper.getMainLooper());
        mCachePool = Executors.newCachedThreadPool();

        // JELLY_BEAN(16) has bug that can't set core pool size to 0, otherwise it will not create
        // thread to run task.
        mScheduledPool = Executors.newScheduledThreadPool(4);
        ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) mScheduledPool;
        executor.setKeepAliveTime(3, TimeUnit.SECONDS);
        executor.allowCoreThreadTimeOut(true);
    }

    /**
     * @see ExecutorService#submit(Callable)
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return mCachePool.submit(task);
    }

    /**
     * @see ExecutorService#submit(Runnable)
     */
    public static Future<?> submit(Runnable task) {
        return mCachePool.submit(task);
    }

    /**
     * @see ExecutorService#submit(Runnable, Object)
     */
    public static <T> Future<T> submit(Runnable task, T result) {
        return mCachePool.submit(task, result);
    }

    /**
     * @see ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
     */
    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return mScheduledPool.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    /**
     * @see ScheduledExecutorService#schedule(Runnable, long, TimeUnit)
     */
    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return mScheduledPool.schedule(command, delay, unit);
    }

    /**
     * @see ScheduledExecutorService#schedule(Callable, long, TimeUnit)
     */
    public static <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return mScheduledPool.schedule(callable, delay, unit);
    }

    /**
     * @see ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return mScheduledPool.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /**
     * @see ExecutorService#execute(Runnable)
     */
    public static void execute(Runnable command) {
        mCachePool.execute(command);
    }

    public static void runInUiThread(Runnable runnable) {
        mUiHandler.post(runnable);
    }
}
