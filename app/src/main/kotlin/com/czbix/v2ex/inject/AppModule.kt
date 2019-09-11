package com.czbix.v2ex.inject

import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.model.PreferenceStorage
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.network.V2exService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Singleton
    @Provides
    fun provideContext(app: AppCtx): Context {
        return app.applicationContext
    }

    @Provides
    fun providesWifiManager(context: Context): WifiManager {
        return context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @Provides
    fun providesConnectivityManager(context: Context): ConnectivityManager {
        return context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
    }

    @Provides
    fun providesClipboardManager(context: Context): ClipboardManager {
        return context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE)
                as ClipboardManager
    }

    @Singleton
    @Provides
    fun providesPrefStore(): PrefStore {
        return PrefStore.getInstance()
    }

    @Singleton
    @Provides
    fun providesPreferenceStorage(context: Context): PreferenceStorage {
        return PreferenceStorage.SharedPreferenceStorage(context)
    }

    @Singleton
    @Provides
    fun provideV2exService(): V2exService {
        return V2exService(RequestHelper)
    }
}