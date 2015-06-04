package com.czbix.v2ex.model.url;

import android.content.Context;

import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.czbix.v2ex.AppCtx;

public class GooglePhotoUrlLoader extends BaseGlideUrlLoader<String> {
    private static final GooglePhotoUrlLoader INSTANCE;

    static {
        INSTANCE = new GooglePhotoUrlLoader(AppCtx.getInstance());
    }

    public static GooglePhotoUrlLoader getInstance() {
        return INSTANCE;
    }

    public GooglePhotoUrlLoader(Context context) {
        super(context);
    }

    @Override
    protected String getUrl(String model, int width, int height) {
        return String.format("%s=w%d-h%d", model, width, height);
    }
}
