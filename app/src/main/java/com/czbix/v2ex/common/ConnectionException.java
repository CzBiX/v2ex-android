package com.czbix.v2ex.common;

public class ConnectionException extends Exception {
    public ConnectionException() {
    }

    public ConnectionException(String detailMessage) {
        super(detailMessage);
    }

    public ConnectionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ConnectionException(Throwable throwable) {
        super(throwable);
    }
}
