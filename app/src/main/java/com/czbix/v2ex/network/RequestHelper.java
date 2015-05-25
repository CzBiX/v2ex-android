package com.czbix.v2ex.network;

import android.os.RemoteException;
import android.util.Log;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.common.ConnectionException;
import com.czbix.v2ex.common.FatalException;
import com.czbix.v2ex.common.RequestException;
import com.czbix.v2ex.model.GsonFactory;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.network.interceptor.UserAgentInterceptor;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestHelper {
    private static final String TAG = RequestHelper.class.getSimpleName();
    private static final String BASE_URL = "https://www.v2ex.com/";
    private static final String LATEST_URL = BASE_URL + "api/topics/latest.json";

    public static final String USER_AGENT = "V2EX+ " + BuildConfig.VERSION_NAME;
    public static final int SERVER_ERROR_CODE = 500;

    private static final OkHttpClient CLIENT;

    static {
        CLIENT = new OkHttpClient();
        CLIENT.setCache(buildCache());
        CLIENT.setConnectTimeout(10, TimeUnit.SECONDS);
        CLIENT.setWriteTimeout(10, TimeUnit.SECONDS);
        CLIENT.setReadTimeout(30, TimeUnit.SECONDS);
        CLIENT.networkInterceptors().add(new UserAgentInterceptor());
    }

    private static Cache buildCache() {
        final File cacheDir = AppCtx.getInstance().getCacheDir();
        final int cacheSize = 128 * 1024 * 1024;

        return new Cache(cacheDir, cacheSize);
    }

    public static List<Topic> getLatest() throws ConnectionException, RemoteException, RequestException {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "request latest topic by api");
        }

        final Request request = new Request.Builder()
                .url(LATEST_URL)
                .build();

        final String json = sendRequest(request);
        final List<Topic> topics = GsonFactory.getInstance().fromJson(json, new TypeToken<List<Topic>>() {
        }.getType());

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "received topics, count: " + topics.size());
        }

        return topics;
    }

    private static String sendRequest(Request request) throws ConnectionException, RemoteException {
        final Response response;
        try {
            response = CLIENT.newCall(request).execute();
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        checkResponse(response);

        final String json;
        try {
            json = response.body().string();
        } catch (IOException e) {
            throw new FatalException(e);
        }
        return json;
    }

    public static void checkResponse(Response response) throws RemoteException, RequestException {
        if (response.isSuccessful()) {
            return;
        }

        if (response.code() >= SERVER_ERROR_CODE) {
            throw new RemoteException();
        }

        throw new RequestException();
    }
}
