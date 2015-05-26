package com.czbix.v2ex.network;

import android.os.RemoteException;
import android.util.Log;

import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RequestException;
import com.czbix.v2ex.model.GsonFactory;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.network.interceptor.UserAgentInterceptor;
import com.czbix.v2ex.parser.Parser;
import com.czbix.v2ex.parser.TopicListParser;
import com.czbix.v2ex.util.IOUtils;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestHelper {
    private static final String TAG = RequestHelper.class.getSimpleName();
    private static final String BASE_URL = "https://www.v2ex.com";
    private static final String API_GET_ALL_NODES = BASE_URL + "/api/nodes/all.json";

    private static final int SERVER_ERROR_CODE = 500;

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
        final File cacheDir = IOUtils.getWebCachePath();
        final int cacheSize = 128 * 1024 * 1024;

        return new Cache(cacheDir, cacheSize);
    }

    public static List<Topic> getTopics(Page page) throws ConnectionException, RemoteException {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "request latest topic for page: " + page.getTitle());
        }

        final Request request = new Request.Builder()
                .url(BASE_URL + page.getUrl())
                .build();

        final Response response = sendRequest(request);

        final Document doc;
        final List<Topic> topics;
        try {
            doc = Parser.toDoc(response.body().string());
            topics = TopicListParser.parseDoc(doc, page);
        } catch (IOException e) {
            throw new ConnectionException(e);
        } catch (SAXException e) {
            throw new RequestException(e);
        }

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "received topics, count: " + topics.size());
        }

        return topics;
    }

    public static List<Node> getAllNodes() throws ConnectionException, RemoteException {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "request all nodes");
        }

        final Request request = new Request.Builder().url(API_GET_ALL_NODES).build();
        final Response response = sendRequest(request);

        try {
            final String json = response.body().string();
            return GsonFactory.getInstance().fromJson(json, new TypeToken<List<Node>>() {}.getType());
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    public static byte[] getImage(String url) throws ConnectionException, RemoteException {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "request image: " + url);
        }

        final Request request = new Request.Builder().url(url).build();
        final Response response = sendRequest(request);

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "response image from cache: " + (response.cacheResponse() != null));
        }

        try {
            return response.body().bytes();
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    private static Response sendRequest(Request request) throws ConnectionException, RemoteException {
        final Response response;
        try {
            response = CLIENT.newCall(request).execute();
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        checkResponse(response);

        return response;
    }

    private static void checkResponse(Response response) throws RemoteException, RequestException {
        if (response.isSuccessful()) {
            return;
        }

        if (response.code() >= SERVER_ERROR_CODE) {
            throw new RemoteException();
        }

        throw new RequestException();
    }
}
