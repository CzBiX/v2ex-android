package com.czbix.v2ex.ui.loader;

import android.content.Context;

import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.network.RequestHelper;

import java.util.List;

public class TopicListLoader extends AsyncTaskLoader<List<Topic>> {
    private final Page mPage;

    public TopicListLoader(Context context, Page page) {
        super(context);

        mPage = page;
    }

    @Override
    public List<Topic> loadInBackgroundWithException() throws Exception {
        return RequestHelper.getTopics(mPage);
    }
}
