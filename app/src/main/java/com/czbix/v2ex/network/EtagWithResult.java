package com.czbix.v2ex.network;

public class EtagWithResult<T> {
    public final String mEtag;
    public final T mResult;
    public final boolean isModified;

    EtagWithResult() {
        isModified = false;
        mEtag = null;
        mResult = null;
    }

    EtagWithResult(String etag, T result) {
        isModified = true;
        mEtag = etag;
        mResult = result;
    }
}
