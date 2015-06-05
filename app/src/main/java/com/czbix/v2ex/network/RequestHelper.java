package com.czbix.v2ex.network;

import android.os.RemoteException;
import android.util.Log;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.common.exception.RequestException;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.GsonFactory;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.czbix.v2ex.network.interceptor.UserAgentInterceptor;
import com.czbix.v2ex.parser.MyselfParser;
import com.czbix.v2ex.parser.Parser;
import com.czbix.v2ex.parser.TopicListParser;
import com.czbix.v2ex.parser.TopicParser;
import com.czbix.v2ex.util.IoUtils;
import com.czbix.v2ex.util.LogUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestHelper {
    public static final String BASE_URL = "https://www.v2ex.com";

    private static final String TAG = RequestHelper.class.getSimpleName();
    private static final String API_GET_ALL_NODES = BASE_URL + "/api/nodes/all.json";
    private static final String URL_SIGN_IN = BASE_URL + "/signin";
    private static final String URL_ONCE_CODE = URL_SIGN_IN;

    private static final int SERVER_ERROR_CODE = 500;

    private static final OkHttpClient CLIENT;

    private static V2CookieStore mCookies;

    static {
        CLIENT = new OkHttpClient();
        CLIENT.setCache(buildCache());
        CLIENT.setConnectTimeout(10, TimeUnit.SECONDS);
        CLIENT.setWriteTimeout(10, TimeUnit.SECONDS);
        CLIENT.setReadTimeout(30, TimeUnit.SECONDS);
        CLIENT.networkInterceptors().add(new UserAgentInterceptor());
        CLIENT.setFollowRedirects(false);

        mCookies = new V2CookieStore(AppCtx.getInstance());
        CLIENT.setCookieHandler(new CookieManager(mCookies, null));
    }

    private static Cache buildCache() {
        final File cacheDir = IoUtils.getWebCachePath();
        final int cacheSize = 128 * 1024 * 1024;

        return new Cache(cacheDir, cacheSize);
    }

    public static void clearCookies() {
        mCookies.removeAll();
    }

    public static List<Topic> getTopics(Page page) throws ConnectionException, RemoteException {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "request latest topic for page: " + page.getTitle());
        }

        final Request request = new Request.Builder()
                .url(page.getUrl())
                .build();

        final Response response = sendRequest(request);

        final Document doc;
        final List<Topic> topics;
        try {
            doc = Parser.toDoc(response.body().string());
            topics = TopicListParser.parseDoc(doc, page);
            processUserState(doc);
        } catch (IOException e) {
            throw new ConnectionException(e);
        } catch (SAXException e) {
            throw new RequestException(response, e);
        }

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "received topics, count: " + topics.size());
        }

        return topics;
    }

    public static TopicWithComments getTopicWithComments(Topic topic) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "request topic with comments, id: %d, title: %s", topic.getId(), topic.getTitle());

        final Request request = new Request.Builder()
                .url(topic.getUrl())
                .build();
        final Response response = sendRequest(request);

        final Document doc;
        final TopicWithComments result;

        try {
            doc = Parser.toDoc(response.body().string());
            result = TopicParser.parseDoc(doc, topic);
            processUserState(doc);
        } catch (SAXException e) {
            throw new RequestException(response, e);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }

        return result;
    }

    private static void processUserState(Document doc) {
        if (UserState.getInstance().isAnonymous()) {
            return;
        }

        final MyselfParser.MySelfInfo info = MyselfParser.parseDoc(doc);
        UserState.getInstance().handleInfo(info);
    }

    public static List<Node> getAllNodes(Etag etag) throws ConnectionException, RemoteException {
        Preconditions.checkNotNull(etag);

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "request all nodes");
        }

        final Request request = new Request.Builder().url(API_GET_ALL_NODES).build();
        final Response response = sendRequest(request);

        final String newEtag = response.header(HttpHeaders.ETAG);
        if (!etag.setNewEtag(newEtag)) {
            return null;
        }

        try {
            final String json = response.body().string();

            return GsonFactory.getInstance().fromJson(json, new TypeToken<List<Node>>() {
            }.getType());
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    public static boolean reply(Topic topic, String content, String once) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "reply to topic: %s", topic.getTitle());

        if (Strings.isNullOrEmpty(once)) {
            once = getOnceCode();
        }
        final RequestBody requestBody = new FormEncodingBuilder().add("once", once)
                .add("content", content)
                .build();

        final Request request = new Request.Builder().url(topic.getUrl())
                .post(requestBody).build();
        final Response response = sendRequest(request, false);

        // v2ex will redirect if reply success
        return response.code() == 302;
    }

    public static void ignore(Comment comment, String onceToken) throws ConnectionException, RemoteException {
        final Request request = new Request.Builder().url(comment.getIgnoreUrl() + "?once=" + onceToken)
                .post(null).build();

        sendRequest(request);
    }

    public static void thank(Comment comment, String csrfToken) throws ConnectionException, RemoteException {
        final Request request = new Request.Builder().url(comment.getThankUrl() + "?t=" + csrfToken)
                .post(null).build();

        sendRequest(request);
    }

    public static Avatar login(String account, String password) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "login user: " + account);

        final String onceCode = getOnceCode();
        final String nextUrl = "/settings";
        final RequestBody requestBody = new FormEncodingBuilder().add("once", onceCode)
                .add("u", account)
                .add("p", password)
                .add("next", nextUrl)
                .build();
        Request request = new Request.Builder().url(URL_SIGN_IN)
                .header(HttpHeaders.REFERER, URL_SIGN_IN)
                .post(requestBody).build();
        Response response = sendRequest(request, false);

        // v2ex will redirect if login success
        if (response.code() != 302) {
            return null;
        }

        final String location = response.header(HttpHeaders.LOCATION);
        if (!location.equals(nextUrl)) {
            return null;
        }

        try {
            request = new Request.Builder().url(new URL(request.url(), location)).build();
        } catch (MalformedURLException e) {
            throw new FatalException(e);
        }
        response = sendRequest(request);

        try {
            final String html = response.body().string();
            final Document document = Parser.toDoc(html);
            return MyselfParser.parseAvatarOnly(document);
        } catch (IOException e) {
            throw new ConnectionException(e);
        } catch (SAXException e) {
            throw new RequestException(response, e);
        }
    }

    public static String getOnceCode() throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "get once code");

        final Request request = new Request.Builder().url(URL_ONCE_CODE).build();
        final Response response = sendRequest(request);

        try {
            final String html = response.body().string();
            return Parser.parseOnceCode(html);
        } catch (IOException e) {
            throw new ConnectionException(e);
        } catch (SAXException e) {
            throw new RequestException(response, e);
        }
    }

    private static Response sendRequest(Request request) throws ConnectionException, RemoteException {
        return sendRequest(request, true);
    }

    private static Response sendRequest(Request request, boolean checkResponse) throws ConnectionException, RemoteException {
        final Response response;
        try {
            response = CLIENT.newCall(request).execute();
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        if (checkResponse) {
            checkResponse(response);
        }

        return response;
    }

    private static void checkResponse(Response response) throws RemoteException, RequestException {
        if (response.isSuccessful()) {
            return;
        }

        if (response.code() >= SERVER_ERROR_CODE) {
            throw new RemoteException();
        }

        throw new RequestException(response);
    }
}
