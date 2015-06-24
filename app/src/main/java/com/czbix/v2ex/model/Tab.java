package com.czbix.v2ex.model;

import android.os.Parcel;

import com.czbix.v2ex.network.RequestHelper;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class Tab extends Page {
    private static final String[][] TAB_DATA = {
            {"全部", "all"},
            {"最热", "hot"},
            {"技术", "tech"},
            {"创意", "creative"},
            {"好玩", "play"},
            {"Apple", "apple"},
            {"酷工作", "jobs"},
            {"交易", "deals"},
            {"城市", "city"},
            {"问与答", "qna"},
            {"R2", "r2"},
            //{"", "nodes"}, // why it's empty?
            {"关注", "members"},
    };
    public static final ImmutableMap<String, Tab> ALL_TABS;
    private static final String SEPARATOR = ",";

    static {
        final ImmutableMap.Builder<String, Tab> builder = ImmutableMap.builder();
        for (String[] data : TAB_DATA) {
            final String title = data[0];
            final String key = data[1];
            final Tab tab = new Tab(title, key);
            builder.put(key, tab);
        }

        ALL_TABS = builder.build();
    }

    private final String mTitle;
    private final String mKey;

    public Tab(String title, String key) {
        mTitle = title;
        mKey = key;
    }

    private Tab(Parcel in) {
        mTitle = in.readString();
        mKey = in.readString();
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getUrl() {
        return RequestHelper.BASE_URL + "/?tab=" + mKey;
    }

    public String getKey() {
        return mKey;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mKey);
    }

    public static final Creator<Tab> CREATOR = new Creator<Tab>() {
        public Tab createFromParcel(Parcel source) {
            return new Tab(source);
        }

        public Tab[] newArray(int size) {
            return new Tab[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tab)) return false;
        Tab tab = (Tab) o;
        return Objects.equal(mTitle, tab.mTitle) &&
                Objects.equal(mKey, tab.mKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mKey);
    }

    /**
     * you should cache the result to avoid overhead
     */
    public static List<Tab> getTabsToShow(String prefStr) {
        if (Strings.isNullOrEmpty(prefStr)) {
            return Lists.newArrayList(ALL_TABS.values());
        }

        final String[] keys = prefStr.split(SEPARATOR);
        final ArrayList<Tab> result = Lists.newArrayList();
        for (String key : keys) {
            result.add(ALL_TABS.get(key));
        }

        return result;
    }

    public static String getStringToSave(List<Tab> tabs) {
        return Joiner.on(SEPARATOR).join(Lists.transform(tabs, new Function<Tab, String>() {
            @Override
            public String apply(Tab input) {
                return input.getKey();
            }
        }));
    }
}
