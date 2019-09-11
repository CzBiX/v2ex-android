package com.czbix.v2ex.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.event.BaseEvent
import com.czbix.v2ex.ui.model.NightMode
import com.google.common.eventbus.Subscribe
import dagger.android.support.DaggerAppCompatActivity

abstract class BaseActivity : DaggerAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val wrapper = Wrapper()
        AppCtx.eventBus.register(wrapper)

        if (!AppCtx.instance.isInited.isLocked) {
            AppCtx.eventBus.unregister(wrapper)
            return
        }

        startActivityForResult(Intent(this, LoadingActivity::class.java), REQ_CODE_LOADING)
    }

    private inner class Wrapper {
        @Subscribe
        fun onContextInitFinishEvent(e: BaseEvent.ContextInitFinishEvent) {
            AppCtx.eventBus.unregister(this)

            finishActivity(REQ_CODE_LOADING)
        }
    }

    protected fun initNightMode(mode: LiveData<NightMode>) {
        mode.observe(this) {
            updateNightMode(it)
        }
    }

    private fun updateNightMode(mode: NightMode) {
        val modeToSet = when (mode) {
            NightMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            NightMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            NightMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        delegate.localNightMode = modeToSet
    }

    companion object {
        private val REQ_CODE_LOADING = 0
    }
}
