package com.czbix.v2ex.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.czbix.v2ex.common.exception.NotImplementedException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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

    /**
     * Construct a persistent cookie store.
     *
     * @param context
     *            Context to attach cookie store to
     */
    public V2CookieStore(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        mCache = Maps.newHashMap();

        loadFromStore();
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
            strings.add(encodeCookie(new SerializableHttpCookie(httpCookie)));
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
    protected String encodeCookie(SerializableHttpCookie cookie) {
        if (cookie == null)
            return null;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (IOException e) {
            Log.d(TAG, "IOException in encodeCookie", e);
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    /**
     * Returns HttpCookie decoded from cookie string
     *
     * @param cookieString
     *            string of cookie as returned from http request
     * @return decoded cookie or null if exception occurred
     */
    protected HttpCookie decodeCookie(String cookieString) {
        byte[] bytes = hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                bytes);

        HttpCookie cookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(
                    byteArrayInputStream);
            cookie = ((SerializableHttpCookie) objectInputStream.readObject())
                    .getCookie();
        } catch (IOException e) {
            Log.d(TAG, "IOException in decodeCookie", e);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "ClassNotFoundException in decodeCookie", e);
        }

        return cookie;
    }

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't
     * have to rely on any large Base64 libraries. Can be overridden if you
     * like!
     *
     * @param bytes
     *            byte array to be converted
     * @return string containing hex values
     */
    protected String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * Converts hex values from strings to byte array
     *
     * @param hexString
     *            string of hex-encoded values
     * @return decoded byte array
     */
    protected byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private static class SerializableHttpCookie implements Serializable {
        private static final long serialVersionUID = -6051428667568260064L;

        private transient HttpCookie cookie;

        public SerializableHttpCookie(HttpCookie cookie) {
            this.cookie = cookie;
        }

        public HttpCookie getCookie() {
            return cookie;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(cookie.getName());
            out.writeObject(cookie.getValue());
            out.writeObject(cookie.getComment());
            out.writeObject(cookie.getCommentURL());
            out.writeBoolean(cookie.getDiscard());
            out.writeObject(cookie.getDomain());
            out.writeLong(cookie.getMaxAge());
            out.writeObject(cookie.getPath());
            out.writeObject(cookie.getPortlist());
            out.writeBoolean(cookie.getSecure());
            out.writeInt(cookie.getVersion());
        }

        private void readObject(ObjectInputStream in) throws IOException,
                ClassNotFoundException {
            String name = (String) in.readObject();
            String value = (String) in.readObject();
            cookie = new HttpCookie(name, value);
            cookie.setComment((String) in.readObject());
            cookie.setCommentURL((String) in.readObject());
            cookie.setDiscard(in.readBoolean());
            cookie.setDomain((String) in.readObject());
            cookie.setMaxAge(in.readLong());
            cookie.setPath((String) in.readObject());
            cookie.setPortlist((String) in.readObject());
            cookie.setSecure(in.readBoolean());
            cookie.setVersion(in.readInt());
        }
    }
}
