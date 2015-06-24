package com.czbix.v2ex.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.czbix.v2ex.AppCtx;
import com.czbix.v2ex.R;
import com.czbix.v2ex.network.RequestHelper;

public abstract class Page implements Parcelable {
    public static final Page PAGE_FAV_TOPIC = new SimplePage(
            AppCtx.getInstance().getString(R.string.title_fragment_favorite), "/my/topics");

    public abstract String getTitle();

    public abstract String getUrl();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public static class SimplePage extends Page {
        private final String mTitle;
        private final String mUrl;

        public SimplePage(String title, String url) {
            mTitle = title;
            mUrl = url;
        }

        @Override
        public String getTitle() {
            return mTitle;
        }

        @Override
        public String getUrl() {
            return RequestHelper.BASE_URL + mUrl;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mTitle);
            dest.writeString(this.mUrl);
        }

        protected SimplePage(Parcel in) {
            this.mTitle = in.readString();
            this.mUrl = in.readString();
        }

        public static final Creator<SimplePage> CREATOR = new Creator<SimplePage>() {
            public SimplePage createFromParcel(Parcel source) {
                return new SimplePage(source);
            }

            public SimplePage[] newArray(int size) {
                return new SimplePage[size];
            }
        };
    }
}
