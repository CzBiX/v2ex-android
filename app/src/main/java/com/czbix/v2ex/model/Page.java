package com.czbix.v2ex.model;

import android.os.Parcelable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public abstract class Page implements Parcelable {
    private final String mTitle;

    Page(String title) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title));

        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public abstract String getUrl();

    @Override
    public int describeContents() {
        return 0;
    }
}
