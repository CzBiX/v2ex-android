package com.czbix.v2ex.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.czbix.v2ex.common.exception.NotImplementedException;
import com.czbix.v2ex.util.LogUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * please to use {@link java.net.CookieManager} with this store.
 * This implement not check the path and host etc.
 */
public class V2CookieStore implements CookieStore {
    private static final String TAG = V2CookieStore.class.getSimpleName();
    private static final String COOKIE_PREFS = "CookiePrefsFile";
    private static final String COOKIE_NAME_STORE = "hosts";
    private static final String COOKIE_NAME_PREFIX = "cookie_";
    private static final String HOST_DELIMITER = ",";

    private final Map<String, Set<HttpCookie>> mCache;
    private final SharedPreferences cookiePrefs;
    private final Gson mGson;

    /**
     * Construct a persistent cookie store.
     *
     * @param context
     *            Context to attach cookie store to
     */
    public V2CookieStore(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        mCache = Maps.newHashMap();
        mGson = new Gson();

        try {
            loadFromStore();
        } catch (Exception e) {
            LogUtils.w(TAG, "read cookies failed, remove all", e);
            removeAll();
        }
    }

    private void loadFromStore() {
        String storedCookieNames = cookiePrefs.getString(COOKIE_NAME_STORE, null);
        if (storedCookieNames == null) {
            return;
        }

        String[] hostNames = TextUtils.split(storedCookieNames, HOST_DELIMITER);
        for (String host : hostNames) {
            Set<String> encodedCookie = cookiePrefs.getStringSet(COOKIE_NAME_PREFIX
                    + host, null);
            if (encodedCookie == null) {
                continue;
            }
            Set<HttpCookie> cookies = Sets.newHashSet();
            for (String s : encodedCookie) {
                HttpCookie decodedCookie = decodeCookie(s);
                if (decodedCookie != null) {
                    cookies.add(decodedCookie);
                }
            }
            mCache.put(host, cookies);
        }
    }

    public synchronized void add(URI uri, HttpCookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("cookie == null");
        }

        final String host = uri.getHost();
        Set<HttpCookie> cookies = mCache.get(host);
        boolean newHost;
        if (cookies == null) {
            cookies = Sets.newHashSet();
            mCache.put(host, cookies);
            newHost = true;
        } else {
            cookies.remove(cookie);
            newHost = false;
        }
        cookies.add(cookie);

        // Save cookie into persistent store
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        if (newHost) {
            prefsWriter.putString(COOKIE_NAME_STORE,
                    TextUtils.join(HOST_DELIMITER, mCache.keySet()));
        }

        Set<String> strings = Sets.newHashSet();
        for (HttpCookie httpCookie : cookies) {
            strings.add(encodeCookie(httpCookie));
        }

        prefsWriter.putStringSet(COOKIE_NAME_PREFIX + host, strings);
        prefsWriter.apply();
    }

    public synchronized List<HttpCookie> get(URI uri) {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }
        final String host = uri.getHost();

        List<HttpCookie> result = new ArrayList<>();
        // get all cookies that domain matches the URI
        for (Set<HttpCookie> cookies : mCache.values()) {
            for (Iterator<HttpCookie> i = cookies.iterator(); i.hasNext();) {
                HttpCookie cookie = i.next();
                if (!HttpCookie.domainMatches(cookie.getDomain(), host)) {
                    continue;
                }
                if (cookie.hasExpired()) {
                    i.remove(); // remove expired cookies
                } else if (!result.contains(cookie)) {
                    result.add(cookie);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<HttpCookie> getCookies() {
        throw new NotImplementedException();
    }

    public synchronized List<URI> getURIs() {
        throw new NotImplementedException();
    }

    public synchronized boolean remove(URI uri, HttpCookie cookie) {
        throw new NotImplementedException();
    }

    public synchronized int getHostSize() {
        return mCache.size();
    }

    public synchronized boolean removeAll() {
        cookiePrefs.edit().clear().apply();
        boolean result = !mCache.isEmpty();
        mCache.clear();
        return result;
    }

    /**
     * Serializes HttpCookie object into String
     *
     * @param cookie
     *            cookie to be encoded, can be null
     * @return cookie encoded as String
     */
    protected String encodeCookie(HttpCookie cookie) {
        if (cookie == null)
            return null;

        return mGson.toJson(cookie);
    }

    /**
     * Returns HttpCookie decoded from cookie string
     *
     * @param cookieString
     *            string of cookie as returned from http request
     * @return decoded cookie or null if exception occurred
     */
    protected HttpCookie decodeCookie(String cookieString) {
        return mGson.fromJson(cookieString, HttpCookie.class);
    }
}
