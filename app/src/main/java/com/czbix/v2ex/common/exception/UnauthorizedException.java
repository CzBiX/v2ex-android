package com.czbix.v2ex.common.exception;


import okhttp3.Response;

public class UnauthorizedException extends RequestException {
    public UnauthorizedException(Response response) {
        super(response);
    }

    public UnauthorizedException(Response response, Throwable tr) {
        super(response, tr);
    }
}
