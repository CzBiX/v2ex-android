package com.czbix.v2ex.ui.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.RemoteException;

import com.czbix.v2ex.common.ConnectionException;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.network.RequestHelper;

import java.util.List;

public class TopicLoader extends AsyncTaskLoader<List<Topic>> {
    private List<Topic> mResult;

    public TopicLoader(Context context) {
        super(context);
    }

    @Override
    public List<Topic> loadInBackground() {
        try {
            mResult = RequestHelper.getLatest();
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
