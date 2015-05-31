package com.czbix.v2ex.ui.loader;

import android.content.Context;
import android.os.RemoteException;

import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.network.RequestHelper;

import java.util.List;

public class TopicListLoader extends AsyncTaskLoader<List<Topic>> {
    private final Page mPage;
    private List<Topic> mResult;
    private boolean mNeedRefresh;

    public TopicListLoader(Context context, Page page) {
        super(context);

        mPage = page;
        mNeedRefresh = true;
    }

    @Override
    protected void onStartLoading() {
        if (!mNeedRefresh) {
            // FIXME: a trick to avoid reload
            return;
        }
        super.onStartLoading();
    }

    @Override
    public List<Topic> loadInBackground() {
        try {
            mResult = RequestHelper.getTopics(mPage);
        } catch (ConnectionException | RemoteException e) {
            e.printStackTrace();
        }
        return mResult;
    }

    @Override
    public boolean takeContentChanged() {
        return mNeedRefresh;
    }

    @Override
    public void deliverResult(List<Topic> data) {
        super.deliverResult(data);

        mNeedRefresh = false;
    }
}
