package com.czbix.v2ex.ui.settings

import androidx.lifecycle.ViewModel
import com.czbix.v2ex.model.PreferenceStorage
import com.czbix.v2ex.ui.model.NightModeDelegate
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
        private val nightModeDelegate: NightModeDelegate,
        private val prefs: PreferenceStorage
) : ViewModel() {
    val nightMode
        get() = nightModeDelegate.currentNightMode.key

    fun setNightMode(key: String) {
        prefs.nightMode = key
    }
}
