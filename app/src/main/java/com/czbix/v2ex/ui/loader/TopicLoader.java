package com.czbix.v2ex.ui.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.RemoteException;

import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.network.RequestHelper;

import java.util.List;

public class TopicLoader extends AsyncTaskLoader<List<Topic>> {
    private final Page mPage;
    private List<Topic> mResult;

    public TopicLoader(Context context, Page page) {
        super(context);

        mPage = page;
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
    protected void onStartLoading() {
        super.onStartLoading();

        if (mResult != null && isStarted()) {
            deliverResult(mResult);
        }

        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();

        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        mResult = null;
    }
}
