package com.czbix.v2ex.ui.loader;

import android.content.Context;

import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.czbix.v2ex.network.RequestHelper;

public class TopicLoader extends AsyncTaskLoader<TopicWithComments> {
    private final Topic mTopic;
    private int mPage;

    public TopicLoader(Context context, Topic topic) {
        super(context);

        mTopic = topic;
        mPage = 1;
    }

    public void setPage(int page) {
        if (page == mPage) {
            return;
        }

        mPage = page;
        onContentChanged();
    }

    @Override
    public TopicWithComments loadInBackgroundWithException() throws ConnectionException, RemoteException {
        return RequestHelper.getTopicWithComments(mTopic, mPage);
    }
}
