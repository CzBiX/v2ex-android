package com.czbix.v2ex.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.R
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

    protected fun setupTransparentNavigationBar() {
        val contextView: View = findViewById(R.id.content_view) ?: return
        contextView.setOnApplyWindowInsetsListener { v, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (insets.systemGestureInsets.left != insets.systemWindowInsetLeft
                        || insets.systemGestureInsets.right != insets.systemWindowInsetRight) {
                    // gesture enabled
                    contextView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                    window.navigationBarColor = Color.TRANSPARENT
                }
            }

            (v.layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = insets.systemWindowInsetTop
                leftMargin = insets.systemWindowInsetLeft
                rightMargin = insets.systemWindowInsetRight

                // handle keyboard height
                bottomMargin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (insets.systemWindowInsetBottom > insets.systemGestureInsets.bottom) {
                        insets.systemWindowInsetBottom
                    } else {
                        insets.tappableElementInsets.bottom
                    }
                } else {
                    insets.systemWindowInsetBottom
                }
            }
            insets.consumeSystemWindowInsets()
        }
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
