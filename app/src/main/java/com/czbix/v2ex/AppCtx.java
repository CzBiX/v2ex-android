package com.czbix.v2ex;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.czbix.v2ex.common.NotificationStatus;
import com.czbix.v2ex.common.PrefStore;
import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.ConnectionException;
import com.czbix.v2ex.common.exception.RemoteException;
import com.czbix.v2ex.dao.ConfigDao;
import com.czbix.v2ex.dao.DraftDao;
import com.czbix.v2ex.dao.NodeDao;
import com.czbix.v2ex.dao.V2exDb;
import com.czbix.v2ex.eventbus.BaseEvent;
import com.czbix.v2ex.eventbus.executor.HandlerExecutor;
import com.czbix.v2ex.google.GoogleHelper;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.network.Etag;
import com.czbix.v2ex.network.RequestHelper;
import com.czbix.v2ex.util.ExecutorUtils;
import com.czbix.v2ex.util.LogUtils;
import com.czbix.v2ex.util.TrackerUtils;
import com.czbix.v2ex.util.UserUtils;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.List;

import io.fabric.sdk.android.Fabric;

public class AppCtx extends Application {
    private static final String TAG = AppCtx.class.getSimpleName();

    private static AppCtx mInstance;
    private EventBus mEventBus;
    private volatile boolean mIsInited;

    @Override
    public void onCreate() {
        super.onCreate();
        initCrashlytics();

        mInstance = this;
        init();
    }

    private void initCrashlytics() {
        final Crashlytics.Builder builder = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build());
        Fabric.with(this, builder.build());
    }

    public boolean isInited() {
        return mIsInited;
    }

    private void init() {
        // event bus is the first
        mEventBus = new AsyncEventBus(new HandlerExecutor());
        mEventBus.register(this);

        TrackerUtils.init(this);
        ExecutorUtils.execute(new AsyncInitTask());
    }

    @Subscribe
    public void onDeadEvent(DeadEvent e) {
        final BaseEvent event = (BaseEvent) e.getEvent();
        LogUtils.i(TAG, "dead event: %s", event.toString());
    }

    public static EventBus getEventBus() {
        return mInstance.mEventBus;
    }

    public static AppCtx getInstance() {
        return mInstance;
    }

    public void waitUntilInited() {
        while (!mIsInited) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LogUtils.v(TAG, "wait inited failed", e);
                return;
            }
        }
    }

    private class AsyncInitTask implements Runnable {
        @Override
        public void run() {
            V2exDb.getInstance().init();

            UserState.getInstance().init();
            NotificationStatus.getInstance().init();

            mIsInited = true;
            mEventBus.post(new BaseEvent.ContextInitFinishEvent());

            DraftDao.cleanExpired();
            loadAllNodes();
            UserUtils.checkDailyAward();
            updateGCMToken();
        }

        private void loadAllNodes() {
            final String etagStr = ConfigDao.get(ConfigDao.KEY_NODE_ETAG, null);

            Etag etag = new Etag(etagStr);
            List<Node> result;
            try {
                result = RequestHelper.getAllNodes(etag);
            } catch (ConnectionException | RemoteException e) {
                // just ignore it
                LogUtils.w(TAG, "fetch all nodes failed");
                return;
            }

            if (etag.isModified()) {
                NodeDao.putAll(result);
                ConfigDao.put(ConfigDao.KEY_NODE_ETAG, etag.getNewEtag());
            } else {
                LogUtils.d(TAG, "nodes not modified");
            }
            LogUtils.d(TAG, "load nodes finish!");
        }

        private void updateGCMToken() {
            startService(GoogleHelper.getRegistrationIntentToStartService(AppCtx.this,
                    PrefStore.getInstance().shouldReceiveNotifications()));
        }
    }
}
