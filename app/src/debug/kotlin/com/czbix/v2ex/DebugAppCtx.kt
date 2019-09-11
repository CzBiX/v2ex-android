package com.czbix.v2ex

import com.facebook.stetho.Stetho

@Suppress("unused")
class DebugAppCtx : AppCtx() {
    override fun init() {
        Stetho.initializeWithDefaults(this)

        super.init()
    }

    override val debugHelpers = DebugHelpersImpl()
}