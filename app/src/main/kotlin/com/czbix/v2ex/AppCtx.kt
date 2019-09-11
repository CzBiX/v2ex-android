package com.czbix.v2ex

import android.app.Application
import android.os.Build
import android.os.StrictMode
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.czbix.v2ex.common.NotificationStatus
import com.czbix.v2ex.common.UpdateInfo
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.common.exception.RequestException
import com.czbix.v2ex.dao.ConfigDao
import com.czbix.v2ex.dao.NodeDao
import com.czbix.v2ex.dao.V2exDb
import com.czbix.v2ex.event.BaseEvent
import com.czbix.v2ex.eventbus.executor.HandlerExecutor
import com.czbix.v2ex.inject.AppInjector
import com.czbix.v2ex.network.CzRequestHelper
import com.czbix.v2ex.network.Etag
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.util.*
import com.google.common.eventbus.AsyncEventBus
import com.google.common.eventbus.DeadEvent
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.fabric.sdk.android.Fabric
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import timber.log.Timber
import javax.inject.Inject

open class AppCtx : Application(), HasAndroidInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private lateinit var mEventBus: EventBus
    var isInited = Mutex(true)

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
        super.onCreate()
        initCrashlytics()

        instance = this

        val tree = if (BuildConfig.DEBUG) Timber.DebugTree() else CrashlyticsTree()
        Timber.plant(tree)

        AppInjector.init(this)
        init()
    }

    private fun initCrashlytics() {
        val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        val crashlytics = Crashlytics.Builder().core(core).build()
        Fabric.with(this, crashlytics)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    protected open fun init() {
        // event bus is the first
        mEventBus = AsyncEventBus(HandlerExecutor())
        mEventBus.register(this)

        TrackerUtils.init(this)
        RequestHelper.setLang()
        ExecutorUtils.execute(AsyncInitTask())
    }

    private fun enableStrictMode() {
        StrictMode.ThreadPolicy.Builder().apply {
            detectAll()
            if (Build.BRAND == "samsung") {
                // A lots of read disk operations in Samsung ROM
                permitDiskReads()
            }
            penaltyFlashScreen()
            penaltyLog()
        }.also {
            StrictMode.setThreadPolicy(it.build())
        }
    }

    @Subscribe
    fun onDeadEvent(e: DeadEvent) {
        val event = e.event as BaseEvent
        LogUtils.i(TAG, "dead event: %s", event.toString())
    }

    fun waitUntilInited() {
        if (!isInited.isLocked) {
            return
        }

        runBlocking(Dispatchers.Default) {
            isInited.lock()
            isInited.unlock()
        }
    }

    open val debugHelpers = DebugHelpers()

    private inner class AsyncInitTask : Runnable {
        override fun run() {
            V2exDb.getInstance().init()

            UserState.init()
            NotificationStatus.init()

            isInited.unlock()
            mEventBus.post(BaseEvent.ContextInitFinishEvent())

//            updateServerConfig()
            UserUtils.checkDailyAward()
            loadAllNodes()
//            updateGCMToken()
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
                    is ConnectionException, is RemoteException, is RequestException -> {
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
//            startService(GoogleHelper.getRegistrationIntentToStartService(this@AppCtx,
//                    PrefStore.getInstance().shouldReceiveNotifications()))
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
