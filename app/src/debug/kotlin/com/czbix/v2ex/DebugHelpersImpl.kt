package com.czbix.v2ex

import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient

class DebugHelpersImpl : DebugHelpers() {
    override fun addStethoInterceptor(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return builder.addNetworkInterceptor(StethoInterceptor())
    }
}
