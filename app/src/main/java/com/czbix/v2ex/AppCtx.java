package com.czbix.v2ex;

import android.app.Application;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.dao.NodeDao;
import com.czbix.v2ex.eventbus.BusEvent;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.network.Etag;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.HandlerExecutor;
import com.czbix.v2ex.util.LogUtils;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.List;

public class AppCtx extends Application {
    private static final String TAG = AppCtx.class.getSimpleName();

    private static AppCtx mInstance;
    private float mDensity;
    private EventBus mEventBus;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        init();
    }

    private void init() {
        mEventBus = new AsyncEventBus(new HandlerExecutor());
        mEventBus.register(this);
        mDensity = getDensity(this);

        ExecutorUtils.execute(new AsyncInitTask());
    }

    @Subscribe
    private void onDeadEvent(DeadEvent e) {
        final BusEvent event = (BusEvent) e.getEvent();
        LogUtils.i(TAG, "dead event: %s", event.toString());
    }

    public static EventBus getEventBus() {
        return mInstance.mEventBus;
    }

    private static float getDensity(Context context) {
        final float density = context.getResources().getDisplayMetrics().density;

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "density: " + density);
        }

        return density;
    }

    public static AppCtx getInstance() {
        return mInstance;
    }

    public float getDensity() {
        return mDensity;
    }

    private class AsyncInitTask implements Runnable {
        @Override
        public void run() {
            loadAllNodes();
        }

        private void loadAllNodes() {
            final String etagStr = ConfigDao.get(ConfigDao.KEY_NODE_ETAG, null);

            Etag etag = new Etag(etagStr);
            List<Node> result;
            try {
                result = RequestHelper.getAllNodes(etag);
            } catch (ConnectionException | RemoteException e) {
                // TODO
                e.printStackTrace();
                return;
            }

            if (!etag.isModified()) {
                LogUtils.d(TAG, "nodes not modified");
            } else {
                NodeDao.putAll(result);
                ConfigDao.put(ConfigDao.KEY_NODE_ETAG, etag.getNewEtag());
            }
            LogUtils.d(TAG, "load nodes finish!");
            mEventBus.post(new BusEvent.GetNodesFinishEvent());
        }
    }
}
