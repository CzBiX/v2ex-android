package com.czbix.v2ex.eventbus.executor;

import android.support.annotation.NonNull;

import com.czbix.v2ex.util.ExecutorUtils;

import java.util.concurrent.Executor;

public class HandlerExecutor implements Executor {
    @Override
    public void execute(@NonNull Runnable command) {
        ExecutorUtils.runInUiThread(command);
    }
}
