package com.czbix.v2ex

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.czbix.v2ex.common.NotificationStatus
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.common.UpdateInfo
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.dao.ConfigDao
import com.czbix.v2ex.dao.DraftDao
import com.czbix.v2ex.dao.NodeDao
import com.czbix.v2ex.dao.V2exDb
import com.czbix.v2ex.event.BaseEvent
import com.czbix.v2ex.eventbus.executor.HandlerExecutor
import com.czbix.v2ex.google.GoogleHelper
import com.czbix.v2ex.network.CzRequestHelper
import com.czbix.v2ex.network.Etag
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.util.*
import com.google.common.eventbus.AsyncEventBus
import com.google.common.eventbus.DeadEvent
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import io.fabric.sdk.android.Fabric
import io.reactivex.schedulers.Schedulers

class AppCtx : Application() {
    private lateinit var mEventBus: EventBus
    @Volatile var isInited: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        initCrashlytics()

        instance = this
        init()
    }

    private fun initCrashlytics() {
        val builder = Crashlytics.Builder().core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
        Fabric.with(this, builder.build())
    }

    private fun init() {
        // event bus is the first
        mEventBus = AsyncEventBus(HandlerExecutor())
        mEventBus.register(this)

        TrackerUtils.init(this)
        ExecutorUtils.execute(AsyncInitTask())
    }

    @Subscribe
    fun onDeadEvent(e: DeadEvent) {
        val event = e.event as BaseEvent
        LogUtils.i(TAG, "dead event: %s", event.toString())
    }

    fun waitUntilInited() {
        while (!isInited) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                LogUtils.v(TAG, "wait inited failed", e)
                return
            }

        }
    }

    private inner class AsyncInitTask : Runnable {
        override fun run() {
            V2exDb.getInstance().init()

            UserState.init()
            NotificationStatus.init()

            isInited = true
            mEventBus.post(BaseEvent.ContextInitFinishEvent())

            updateServerConfig()
            UserUtils.checkDailyAward()
            DraftDao.cleanExpired()
            loadAllNodes()
            updateGCMToken()
        }

        private fun loadAllNodes() {
            val etagStr = ConfigDao.get(ConfigDao.KEY_NODE_ETAG, getString(R.string.all_nodes_etag))

            val etag = Etag(etagStr)
            RequestHelper.getAllNodes(etag).subscribe({ result ->
                if (etag.isModified) {
                    NodeDao.putAll(result)
                    ConfigDao.put(ConfigDao.KEY_NODE_ETAG, etag.newEtag)
                } else {
                    LogUtils.d(TAG, "nodes not modified")
                }
                LogUtils.d(TAG, "load nodes finish!")
            }, { error ->
                when (error) {
                    is ConnectionException, is RemoteException -> {
                        LogUtils.w(TAG, "fetch all nodes failed", error)
                    }
                    else -> throw error
                }
            })
        }

        private fun updateServerConfig() {
            CzRequestHelper.getServerConfig().observeOn(Schedulers.computation()).subscribe({ config ->
                UpdateInfo.parseVersionData(config.version)
            }, { throwable ->
                LogUtils.i(TAG, "update server config failed", throwable)
            })
        }

        private fun updateGCMToken() {
            startService(GoogleHelper.getRegistrationIntentToStartService(this@AppCtx,
                    PrefStore.getInstance().shouldReceiveNotifications()))
        }
    }

    companion object {
        private val TAG = getLogTag<AppCtx>()

        @JvmStatic
        lateinit var instance: AppCtx

        @JvmStatic
        val eventBus: EventBus
            get() = instance.mEventBus
    }
}
