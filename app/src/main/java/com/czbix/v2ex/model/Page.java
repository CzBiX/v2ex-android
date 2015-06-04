package com.czbix.v2ex.model;

import android.os.Parcelable;

public abstract class Page implements Parcelable {
    public abstract String getTitle();

    public abstract String getUrl();

    @Override
    public int describeContents() {
        return 0;
    }
}
