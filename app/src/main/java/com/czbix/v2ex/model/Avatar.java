package com.czbix.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.czbix.v2ex.AppCtx;

import java.util.regex.Pattern;

public class Avatar implements Parcelable {
    public static final float DENSITY;
    public static final int SIZE_LARGE = 73;
    public static final int SIZE_NORMAL = 48;
    public static final int SIZE_MINI = 24;

    private static final Pattern PATTERN = Pattern.compile("(mini|normal|large)");
    private static final String LARGE = "large";
    private static final String NORMAL = "normal";
    private static final String MINI = "mini";

    private final String mBaseUrl;

    static {
        DENSITY = AppCtx.getInstance().getResources().getDisplayMetrics().density;
    }

    Avatar(String baseUrl) {
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

    public String getUrlByDp(float dp) {
        final float size = dp * DENSITY;
        if (size >= SIZE_LARGE) {
            return getLarge();
        } else if (size >= SIZE_NORMAL) {
            return getNormal();
        } else {
            return getMini();
        }
    }

    public static String getUrl(String baseUrl, String size) {
        return "https:" + String.format(baseUrl, size);
    }

    private static String getBaseUrl(String url) {
        return PATTERN.matcher(url).replaceFirst("%s");
    }

    public static class Builder {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mBaseUrl);
    }

    protected Avatar(Parcel in) {
        this.mBaseUrl = in.readString();
    }

    public static final Creator<Avatar> CREATOR = new Creator<Avatar>() {
        public Avatar createFromParcel(Parcel source) {
            return new Avatar(source);
        }

        public Avatar[] newArray(int size) {
            return new Avatar[size];
        }
    };
}
