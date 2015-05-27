package com.czbix.v2ex.common.exception;

public class RequestException extends RuntimeException {
    public RequestException() {
    }

    public RequestException(String detailMessage) {
        super(detailMessage);
    }

    public RequestException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RequestException(Throwable throwable) {
        super(throwable);
    }

    public RequestException(int code) {
        this("request failed with code: " + code);
    }
}
