package com.czbix.v2ex.common.exception;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.squareup.okhttp.Response;

public class RequestException extends RuntimeException {
    private final Response mResponse;
    private String errorHtml;
    private boolean mShouldLogged;

    public RequestException(Response response) {
        this(null, response);
    }

    public RequestException(Response response, Throwable tr) {
        this(null, response, tr);
    }

    public RequestException(String message, Response response) {
        this(message, response, null);
    }

    public RequestException(String message, Response response, Throwable tr) {
        super(message, tr);

        mShouldLogged = true;
        mResponse = response;
    }

    /**
     * error info in html
     */
    public String getErrorHtml() {
        return errorHtml;
    }

    public void setErrorHtml(String errorHtml) {
        this.errorHtml = errorHtml;
    }

    public Response getResponse() {
        return mResponse;
    }

    public int getCode() {
        return mResponse.code();
    }

    public boolean isShouldLogged() {
        return mShouldLogged;
    }

    public void setShouldLogged(boolean shouldLogged) {
        mShouldLogged = shouldLogged;
    }

    @Override
    public String getMessage() {
        final String message = Strings.nullToEmpty(super.getMessage());
        final StringBuilder sb = new StringBuilder(message);

        sb.append(", code: ").append(mResponse.code());

        if (mResponse.isRedirect()) {
            sb.append(", location: ");
            sb.append(mResponse.header(HttpHeaders.LOCATION));
        }

        return sb.toString();
    }
}
