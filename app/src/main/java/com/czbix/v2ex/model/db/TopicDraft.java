package com.czbix.v2ex.model.db;

import com.czbix.v2ex.util.GsonUtilsKt;

public class TopicDraft {
    public final String mNodeName;
    public final String mTitle;
    public final String mContent;

    public TopicDraft(String nodeName, String title, String content) {
        mNodeName = nodeName;
        mTitle = title;
        mContent = content;
    }

    public String toJson() {
        return GsonUtilsKt.getGSON().toJson(this);
    }

    public static TopicDraft fromJson(String str) {
        return GsonUtilsKt.getGSON().fromJson(str, TopicDraft.class);
    }
}
