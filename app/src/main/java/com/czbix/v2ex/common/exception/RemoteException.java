package com.czbix.v2ex.common.exception;

import com.squareup.okhttp.Response;

public class RemoteException extends Exception {
    public RemoteException(Response response) {
        this(response, null);
    }

    public RemoteException(Response response, Throwable tr) {
        super("remote failed with code: " + response.code(), tr);
    }
}
