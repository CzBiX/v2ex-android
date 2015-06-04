package com.czbix.v2ex.eventbus.executor;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

public class HandlerExecutor implements Executor {
    private final Handler mHandler;

    public HandlerExecutor() {
        mHandler = new Handler(Looper.myLooper());
    }

    @Override
    public void execute(@NonNull Runnable command) {
        mHandler.post(command);
    }
}
