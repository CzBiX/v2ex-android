package com.czbix.v2ex.util;

import com.czbix.v2ex.AppCtx;

import java.io.File;

public class IoUtils {
    private static File cacheDir;

    static {
        cacheDir = AppCtx.getInstance().getCacheDir();
    }

    public static File getWebCachePath() {
        return new File(cacheDir, "webCache");
    }
}
