package com.czbix.v2ex.common.exception;

public class NotImplementedException extends FatalException {
    public NotImplementedException() {
        super("This method not implemented yet!");
    }
}
