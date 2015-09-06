package com.czbix.v2ex.common.exception;

public class ExIllegalStateException extends java.lang.IllegalStateException {
    public boolean shouldLogged;

    public ExIllegalStateException() {
        super();
    }

    public ExIllegalStateException(Throwable cause) {
        super(cause);
    }

    public ExIllegalStateException(String detailMessage) {
        super(detailMessage);
    }

    public ExIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
