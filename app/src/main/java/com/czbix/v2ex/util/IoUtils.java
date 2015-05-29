package com.czbix.v2ex.util;

import com.czbix.v2ex.AppCtx;

import java.io.File;

public class IoUtils {
    public static File getCachePath() {
        return AppCtx.getInstance().getCacheDir();
    }

    public static File getWebCachePath() {
        return new File(getCachePath(), "webCache");
    }
}
