package com.czbix.v2ex.common;

public class FatalException extends RuntimeException {
    public FatalException(String detailMessage) {
        super(detailMessage);
    }

    public FatalException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FatalException(Throwable throwable) {
        super(throwable);
    }
}
