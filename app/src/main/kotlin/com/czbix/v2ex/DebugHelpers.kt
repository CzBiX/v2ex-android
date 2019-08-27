package com.czbix.v2ex

import okhttp3.OkHttpClient

open class DebugHelpers {
    open fun addStethoInterceptor(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return builder
    }
}