package com.czbix.v2ex.network;

import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.common.exception.RequestException;
import com.czbix.v2ex.util.LogUtils;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import static com.czbix.v2ex.network.RequestHelper.newRequest;

public class CzRequestHelper {
    private static final String BASE_URL = BuildConfig.DEBUG ? "http://192.168.1.103:8000" :"https://v2ex.czbix.com";

    private static final String TAG = CzRequestHelper.class.getSimpleName();
    private static final String API_BASE = BASE_URL + "/api";
    private static final String API_USER = API_BASE + "/user/%s";
    private static final String API_SETTINGS = API_USER + "/settings";

    private static final OkHttpClient CLIENT;

    static {
        CLIENT = RequestHelper.getClient().clone();
        CLIENT.setCookieHandler(null);
    }

    public static void registerUser(String username) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "register user, username: %s", username);

        final Request request = newRequest().url(String.format(API_USER, username))
                .put(RequestBody.create(null, new byte[0])).build();
        final Response response = RequestHelper.sendRequest(request);

        final int code = response.code();
        if (code != HttpStatus.SC_NOT_MODIFIED && code != HttpStatus.SC_CREATED) {
            throw new RequestException("register user failed", response);
        }
    }

    public static void registerDevice(String username, String token) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "register device token: %s", token);

        final RequestBody body = new FormEncodingBuilder()
                .add("action", "add_gcm_token")
                .add("token", token).build();
        final Request request = newRequest().url(String.format(API_SETTINGS, username))
                .post(body).build();

        RequestHelper.sendRequest(request);
    }

    public static void unregisterDevice(String username, String token) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "unregister device token: %s", token);

        final RequestBody body = new FormEncodingBuilder()
                .add("action", "del_gcm_token")
                .add("token", token).build();
        final Request request = newRequest().url(String.format(API_SETTINGS, username))
                .post(body).build();

        RequestHelper.sendRequest(request);
    }

    public static void updateNotificationsToken(String username, String token) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "update notifications token: %s", token);

        final RequestBody body = new FormEncodingBuilder()
                .add("action", "set_ntf_token")
                .add("token", token).build();
        final Request request = newRequest().url(String.format(API_SETTINGS, username))
                .post(body).build();

        RequestHelper.sendRequest(request);
    }
}
