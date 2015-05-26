package com.czbix.v2ex.model;

import java.util.regex.Pattern;

public class Avatar extends Page {
    public static final int SIZE_LARGE = 73;
    public static final int SIZE_NORMAL = 48;
    public static final int SIZE_MINI = 24;

    private static final Pattern PATTERN = Pattern.compile("(mini|normal|large)");
    private static final String LARGE = "large";
    private static final String NORMAL = "normal";
    private static final String MINI = "mini";

    private final String mBaseUrl;

    Avatar(String baseUrl) {
        super("Avatar");

        mBaseUrl = baseUrl;
    }

    public String getLarge() {
        return getUrl(mBaseUrl, LARGE);
    }

    public String getNormal() {
        return getUrl(mBaseUrl, NORMAL);
    }

    public String getMini() {
        return getUrl(mBaseUrl, MINI);
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    @Override
    public String getUrl() {
        return getLarge();
    }

    public static String getUrl(String baseUrl, String size) {
        return String.format(baseUrl, size);
    }

    private static String getBaseUrl(String url) {
        return PATTERN.matcher(url).replaceFirst("%s");
    }

    public static class Builder {
        private String mUrl;
        private String mBaseUrl;

        public Builder setUrl(String url) {
            mBaseUrl = getBaseUrl(url);
            return this;
        }

        public Builder setBaseUrl(String url) {
            mBaseUrl = url;
            return this;
        }

        public Avatar createAvatar() {
            return new Avatar(mBaseUrl);
        }
    }
}
