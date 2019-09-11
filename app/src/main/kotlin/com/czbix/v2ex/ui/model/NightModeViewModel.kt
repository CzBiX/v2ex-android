package com.czbix.v2ex.ui.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.czbix.v2ex.model.PreferenceStorage
import javax.inject.Inject

enum class NightMode(val key: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromKey(key: String): NightMode {
            for (mode in values()) {
                if (mode.key == key) {
                    return mode
                }
            }

            error("Unknown key: $key")
        }
    }
}

class NightModeViewModel @Inject constructor(
        private val nightModeDelegate: NightModeDelegate
) : ViewModel(), NightModeDelegate by nightModeDelegate

interface NightModeDelegate {
    val nightMode: LiveData<NightMode>
    val currentNightMode: NightMode
}

class NightModeDelegateImpl @Inject constructor(
        private val prefs: PreferenceStorage
) : NightModeDelegate {
    override val nightMode by lazy {
        prefs.nightModeObservable.map {
            NightMode.fromKey(it)
        }
    }

    override val currentNightMode
        get() = NightMode.fromKey(prefs.nightMode)
}