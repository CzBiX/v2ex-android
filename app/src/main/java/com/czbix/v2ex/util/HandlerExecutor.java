package com.czbix.v2ex.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

public class HandlerExecutor implements Executor {
    private final Handler mHandler;

    public HandlerExecutor() {
        mHandler = new Handler(Looper.myLooper());
    }

    @Override
    public void execute(Runnable command) {
        mHandler.post(command);
    }
}
