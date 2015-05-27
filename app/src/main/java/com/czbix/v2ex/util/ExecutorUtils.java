package com.czbix.v2ex.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorUtils {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /**
     * @see ExecutorService#submit(Callable)
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return EXECUTOR.submit(task);
    }

    /**
     * @see ExecutorService#submit(Runnable)
     */
    public static Future<?> submit(Runnable task) {
        return EXECUTOR.submit(task);
    }

    /**
     * @see ExecutorService#submit(Runnable, Object)
     */
    public static <T> Future<T> submit(Runnable task, T result) {
        return EXECUTOR.submit(task, result);
    }

    /**
     * @see ExecutorService#execute(Runnable)
     */
    public static void execute(Runnable command) {
        EXECUTOR.execute(command);
    }
}
