package com.czbix.v2ex.model

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.czbix.v2ex.ui.model.NightMode
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface PreferenceStorage {
    val nightModeObservable: LiveData<String>
    var nightMode: String

    @Singleton
    class SharedPreferenceStorage @Inject constructor(
            context: Context
    ) : PreferenceStorage {
        // Use lazy to prevent IO access in UI thread
        private val prefs: Lazy<SharedPreferences> = lazy {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).apply {
                registerOnSharedPreferenceChangeListener(changeListener)
            }
        }
        private val nightModeLiveData = MutableLiveData<String>()

        override var nightMode by StringPreference(prefs, KEY_NIGHT_MODE, NightMode.SYSTEM.key)
        override val nightModeObservable: LiveData<String>
            get() {
                nightModeLiveData.value = nightMode
                return nightModeLiveData.distinctUntilChanged()
            }

        private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            Timber.v("Preference changed: %s", key)

            when (key) {
                KEY_NIGHT_MODE -> nightModeLiveData.value = nightMode
            }
        }

        private class BooleanPreference(
                private val preferences: Lazy<SharedPreferences>,
                private val name: String? = null,
                private val defaultValue: Boolean
        ) : ReadWriteProperty<SharedPreferenceStorage, Boolean> {
            @WorkerThread
            override fun getValue(thisRef: SharedPreferenceStorage, property: KProperty<*>): Boolean {
                return preferences.value.getBoolean(name ?: property.name, defaultValue)
            }

            override fun setValue(thisRef: SharedPreferenceStorage, property: KProperty<*>, value: Boolean) {
                preferences.value.edit { putBoolean(name ?: property.name, value) }
            }
        }

        private class StringPreference(
                private val preferences: Lazy<SharedPreferences>,
                private val name: String? = null,
                private val defaultValue: String
        ) : ReadWriteProperty<SharedPreferenceStorage, String> {
            @WorkerThread
            override fun getValue(thisRef: SharedPreferenceStorage, property: KProperty<*>): String {
                return preferences.value.getString(name ?: property.name, defaultValue)!!
            }

            override fun setValue(thisRef: SharedPreferenceStorage, property: KProperty<*>, value: String) {
                preferences.value.edit { putString(name ?: property.name, value) }
            }
        }

        companion object {
            const val PREFS_NAME = "v2ex"
            const val KEY_NIGHT_MODE = "nightMode"
        }
    }
}