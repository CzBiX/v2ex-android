package com.czbix.v2ex.network;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class V2CookieStoreTest extends AndroidTestCase {
    private static final String COOKIES_STR1 = "test1=value1; domain=.github.com; path=/; expires=Sat, 02 Jun 2035 15:24:58 -0000; secure; HttpOnly";
    private static final String COOKIES_STR2 = "test2=value2; path=/; secure; HttpOnly";

    private Map<String, List<String>> cookiesMap;
    private Context mContext;

    @Override
    public void setUp() throws Exception {
        mContext = getContext();

        cookiesMap = Maps.newHashMap();
        final List<String> list = Lists.newArrayList(COOKIES_STR1, COOKIES_STR2);
        cookiesMap.put("Set-Cookie", list);
    }

    public void testAdd() throws Exception {
        final V2CookieStore store = new V2CookieStore(mContext);
        store.add(URI.create("https://www.v2ex.com"), new HttpCookie("key", "value"));
    }

    public void testGet() throws Exception {
        V2CookieStore store = new V2CookieStore(mContext);
        final CookieManager manager = new CookieManager(store, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        final URI uri = URI.create("https://www.github.com");
        manager.put(uri, cookiesMap);

        List<HttpCookie> readCookies = store.get(uri);
        assertEquals(2, readCookies.size());

        store = new V2CookieStore(mContext);
        readCookies = store.get(uri);
        assertEquals(2, readCookies.size());
    }

    public void testRemoveAll() throws Exception {
        V2CookieStore store = new V2CookieStore(mContext);
        final CookieManager manager = new CookieManager(store, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        final URI uri = URI.create("https://www.github.com");
        manager.put(uri, cookiesMap);

        assertEquals(1, store.getHostSize());

        store.removeAll();
        assertEquals(0, store.getHostSize());
    }

    @Override
    public void tearDown() throws Exception {
        new V2CookieStore(mContext).removeAll();
    }
}