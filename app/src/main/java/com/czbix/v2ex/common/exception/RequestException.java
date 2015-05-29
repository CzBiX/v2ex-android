package com.czbix.v2ex.common.exception;

import com.squareup.okhttp.Response;

public class RequestException extends RuntimeException {
    private final Response mResponse;

    public RequestException(Response response) {
        super("request failed with code: " + response.code());

        mResponse = response;
    }

    public Response getResponse() {
        return mResponse;
    }

    public int getCode() {
        return mResponse.code();
    }
}
