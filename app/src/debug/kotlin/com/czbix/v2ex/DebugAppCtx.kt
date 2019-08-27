package com.czbix.v2ex

import com.facebook.stetho.Stetho

class DebugAppCtx : AppCtx() {
    override fun init() {
        super.init()

        Stetho.initializeWithDefaults(this)
    }

    override val debugHelpers = DebugHelpersImpl()
}