package com.czbix.v2ex.model;

import android.os.Parcel;

public class Tab extends Page.SimplePage {
    public static final Tab[] ALL_TABS = {
            new Tab("全部", "/?tab=all"),
            new Tab("最热", "/?tab=hot"),
            new Tab("技术", "/?tab=tech"),
            new Tab("创意", "/?tab=creative"),
            new Tab("好玩", "/?tab=play"),
            new Tab("Apple", "/?tab=apple"),
            new Tab("酷工作", "/?tab=jobs"),
            new Tab("交易", "/?tab=deals"),
            new Tab("城市", "/?tab=city"),
            new Tab("问与答", "/?tab=qna"),
            new Tab("R2", "/?tab=r2"),
            //new Tab("", "/?tab=nodes"), // why it's empty?
            new Tab("关注", "/?tab=members"),
    };

    protected Tab(Parcel in) {
        super(in);
    }

    public Tab(String title, String url) {
        super(title, url);
    }

    public static final Creator<Tab> CREATOR = new Creator<Tab>() {
        public Tab createFromParcel(Parcel source) {
            return new Tab(source);
        }

        public Tab[] newArray(int size) {
            return new Tab[size];
        }
    };
}
