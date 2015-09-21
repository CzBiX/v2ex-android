package com.czbix.v2ex.network;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.ExIllegalStateException;
import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.common.exception.RequestException;
import com.czbix.v2ex.common.exception.UnauthorizedException;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Favable;
import com.czbix.v2ex.model.GsonFactory;
import com.czbix.v2ex.model.Ignorable;
import com.czbix.v2ex.model.LoginResult;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Notification;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.model.Thankable;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.czbix.v2ex.network.interceptor.UserAgentInterceptor;
import com.czbix.v2ex.parser.MyselfParser;
import com.czbix.v2ex.parser.NotificationParser;
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
import com.squareup.okhttp.ResponseBody;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RequestHelper {
    public static final String BASE_URL = "https://www.v2ex.com";

    private static final String TAG = RequestHelper.class.getSimpleName();
    private static final String API_GET_ALL_NODES = BASE_URL + "/api/nodes/all.json";
    private static final String URL_SIGN_IN = BASE_URL + "/signin";
    private static final String URL_MISSION_DAILY = BASE_URL + "/mission/daily";
    private static final String URL_ONCE_CODE = URL_SIGN_IN;
    private static final String URL_NOTIFICATIONS = BASE_URL + "/notifications";
    private static final String URL_UNREAD_NOTIFICATIONS = BASE_URL + "/mission";
    private static final String URL_NEW_TOPIC = BASE_URL + "/new/%s";

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
        final int cacheSize = 64 * 1024 * 1024;

        return new Cache(cacheDir, cacheSize);
    }

    public static OkHttpClient getClient() {
        return CLIENT;
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
        if (response.isRedirect()) {
            throw new ExIllegalStateException("topics page should not redirect");
        }

        final Document doc;
        final List<Topic> topics;
        try {
            doc = Parser.toDoc(response.body().string());
            processUserState(doc, page instanceof Tab);
            topics = TopicListParser.parseDoc(doc, page);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "received topics, count: " + topics.size());
        }

        return topics;
    }

    public static TopicWithComments getTopicWithComments(Topic topic, int page) throws ConnectionException, RemoteException {
        Preconditions.checkArgument(page > 0, "page must greater than zero");

        LogUtils.v(TAG, "request topic with comments, id: %d, title: %s", topic.getId(), topic.getTitle());

        final Request request = new Request.Builder()
                .url(topic.getUrl() + "?p=" + page)
                .build();
        final Response response = sendRequest(request);
        if (response.isRedirect()) {
            throw new ExIllegalStateException("topic page shouldn't redirect");
        }

        final Document doc;
        final TopicWithComments result;

        try {
            doc = Parser.toDoc(response.body().string());
            processUserState(doc);
            result = TopicParser.parseDoc(doc, topic);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }

        return result;
    }
    private static void processUserState(Document doc) {
        processUserState(doc, false);
    }

    private static void processUserState(Document doc, boolean isTab) {
        if (!UserState.getInstance().isLoggedIn()) {
            return;
        }

        final MyselfParser.MySelfInfo info = MyselfParser.parseDoc(doc, isTab);
        UserState.getInstance().handleInfo(info, isTab);
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

    public static int getUnreadNum() throws ConnectionException, RemoteException {
        Preconditions.checkState(UserState.getInstance().isLoggedIn(), "guest can't check notifications");
        LogUtils.v(TAG, "get unread num");

        final Request request = new Request.Builder().url(URL_UNREAD_NOTIFICATIONS).build();
        final Response response = sendRequest(request);

        try {
            final String html = response.body().string();

            final Document doc = Parser.toDoc(html);
            return NotificationParser.parseUnreadCount(doc);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    public static List<Notification> getNotifications() throws ConnectionException, RemoteException {
        Preconditions.checkState(UserState.getInstance().isLoggedIn(), "guest can't check notifications");
        LogUtils.v(TAG, "get notifications");

        final Request request = new Request.Builder().url(URL_NOTIFICATIONS).build();
        final Response response = sendRequest(request);

        try {
            final String html = response.body().string();

            final Document doc = Parser.toDoc(html);
            return NotificationParser.parseDoc(doc);
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
        return response.code() == HttpStatus.SC_MOVED_TEMPORARILY;
    }

    public static void ignore(Ignorable obj, String onceToken) throws ConnectionException, RemoteException {
        final Request.Builder builder = new Request.Builder().url(obj.getIgnoreUrl() + "?once=" + onceToken);
        final boolean isComment = obj instanceof Comment;
        if (isComment) {
            builder.post(RequestBody.create(null, new byte[0]));
        }
        final Request request = builder.build();

        sendRequest(request, isComment);
    }

    public static void favorite(Favable obj, boolean isFavorite, String csrfToken) throws ConnectionException, RemoteException {
        final String url = isFavorite ? obj.getFavUrl() : obj.getUnFavUrl();
        final Request request = new Request.Builder().url(url + "?t=" + csrfToken)
                .build();

        final Response response = sendRequest(request, false);
        if (!response.isRedirect()) {
            throw new RequestException(String.format("favorite %s failed, is fav: %b", obj,
                    isFavorite), response);
        }
    }

    public static void thank(Thankable obj, String csrfToken) throws ConnectionException, RemoteException {
        final Request request = new Request.Builder().url(obj.getThankUrl() + "?t=" + csrfToken)
                .post(RequestBody.create(null, new byte[0])).build();

        sendRequest(request);
    }

    public static void dailyMission() throws ConnectionException, RemoteException {
        final String onceCode = getOnceCode();
        final Request request = new Request.Builder().url(String.format("%s/redeem?once=%s",
                URL_MISSION_DAILY, onceCode))
                .header(HttpHeaders.REFERER, URL_MISSION_DAILY)
                .build();

        final Response response = sendRequest(request, false);

        if (response.code() != HttpStatus.SC_MOVED_TEMPORARILY) {
            throw new RequestException(response);
        }
    }

    public static int newTopic(String nodeName, String title, String content) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "new topic in node: %s, title: %s", nodeName, title);

        final String once = getOnceCode();
        final RequestBody requestBody = new FormEncodingBuilder().add("once", once)
                .add("title", title)
                .add("content", content)
                .build();

        final Request request = new Request.Builder().url(String.format(URL_NEW_TOPIC, nodeName))
                .post(requestBody).build();
        final Response response = sendRequest(request);

        // v2ex will redirect if reply success
        if (response.code() == HttpStatus.SC_MOVED_TEMPORARILY) {
            final String location = response.header(HttpHeaders.LOCATION);
            return Topic.getIdFromUrl(location);
        }

        final RequestException exception = new RequestException("post new topic failed", response);
        try {
            exception.setErrorHtml(TopicParser.parseProblemInfo(response.body().string()));
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        throw exception;
    }

    public static boolean hasDailyAward() throws ConnectionException, RemoteException {
        final Request request = new Request.Builder().url(URL_MISSION_DAILY).build();

        final Response response = sendRequest(request);

        final String html;
        try {
            html = response.body().string();
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        return MyselfParser.hasAward(html);
    }

    public static LoginResult login(String account, String password) throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "login user: " + account);

        final String onceCode = getOnceCode();
        final String nextUrl = "/mission";
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
        if (response.code() != HttpStatus.SC_MOVED_TEMPORARILY) {
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
            return MyselfParser.parseLoginResult(document);
        } catch (IOException e) {
            throw new ConnectionException(e);
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
        }
    }

    public static String getNotificationsToken() throws ConnectionException, RemoteException {
        LogUtils.v(TAG, "get notifications token");

        final Request request = new Request.Builder().url(URL_NOTIFICATIONS).build();
        final Response response = sendRequest(request);

        try {
            final String html = response.body().string();
            return NotificationParser.parseToken(html);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    static Response sendRequest(Request request) throws ConnectionException, RemoteException {
        return sendRequest(request, true);
    }

    static Response sendRequest(Request request, boolean checkResponse) throws ConnectionException, RemoteException {
        if (BuildConfig.DEBUG && new Random().nextInt(100) > 95) {
            throw new ConnectionException("debug network test");
        }

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

    private static void checkResponse(Response response) throws RemoteException, RequestException, ConnectionException {
        if (response.isSuccessful()) {
            return;
        }

        final int code = response.code();
        if (code >= HttpStatus.SC_MULTIPLE_CHOICES && code < HttpStatus.SC_BAD_REQUEST) {
            if (response.isRedirect() && response.header(HttpHeaders.LOCATION).startsWith("/signin")) {
                throw new UnauthorizedException(response);
            }
            return;
        }

        if (code >= SERVER_ERROR_CODE) {
            throw new RemoteException(response);
        }


        Crashlytics.log("request url: " + response.request().urlString());
        if (code == 403 || code == 404) {
            try {
                final ResponseBody body = response.body();
                if (body.contentLength() != 0) {
                    final String bodyStr = body.string();
                    Crashlytics.log(String.format("response code %d, %s", code,
                            bodyStr.substring(0, Math.min(4096, bodyStr.length()))));
                }
            } catch (IOException e) {
                throw new ConnectionException(e);
            }
        }

        throw new RequestException(response);
    }
}
