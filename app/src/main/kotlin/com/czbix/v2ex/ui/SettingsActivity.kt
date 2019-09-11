package com.czbix.v2ex.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.view.MenuItem
import android.widget.Toast
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.BuildConfig
import com.czbix.v2ex.R
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.event.DeviceRegisterEvent
import com.czbix.v2ex.google.GoogleHelper
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.db.Member
import com.czbix.v2ex.util.MiscUtils
import com.google.common.base.Strings
import io.reactivex.disposables.Disposable

class SettingsActivity : BaseActivity() {
    private val mFragment = PrefsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display the fragment as the main content.
        fragmentManager.beginTransaction().replace(android.R.id.content,
                mFragment).commit()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mFragment.onActivityResult(requestCode, resultCode, data)
    }

    class PrefsFragment : PreferenceFragment(), Preference.OnPreferenceClickListener {
        private var isLogin: Boolean = false
        private lateinit var mNotificationsPref: SwitchPreference
        private var task: Disposable? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general)

            initGeneral()
            initUser()
        }

        private fun initUser() {
            isLogin = UserState.isLoggedIn()
            val user = findPreference(PREF_KEY_CATEGORY_USER) as PreferenceCategory
            if (!isLogin) {
                preferenceScreen.removePreference(user)
                return
            }

            val infoPref = findPreference(PREF_KEY_USER_INFO)
//            mNotificationsPref = findPreference(PREF_KEY_RECEIVE_NOTIFICATIONS) as SwitchPreference
            val logoutPref = findPreference(PREF_KEY_LOGOUT)

            infoPref.summary = UserState.username
            infoPref.setOnPreferenceClickListener {
                MiscUtils.openUrl(activity, Member.buildUrlFromName(
                        UserState.username!!))
                false
            }

//            mNotificationsPref.setOnPreferenceChangeListener {
//                preference, newValue -> toggleReceiveNotifications(newValue as Boolean)
//            }
            logoutPref.onPreferenceClickListener = this
        }

        override fun onStart() {
            super.onStart()

            if (!isLogin) {
                // user not login yet
                return
            }

            val errMsg = GoogleHelper.checkPlayServices(activity)
            if (Strings.isNullOrEmpty(errMsg)) {
//                mNotificationsPref.isEnabled = true
                return
            }
            showPlayServicesErrorToast(errMsg)
//            mNotificationsPref.isEnabled = false
        }

        override fun onStop() {
            super.onStop()

            task?.dispose()
            task = null
        }

        private fun showPlayServicesErrorToast(errMsg: String) {
            Toast.makeText(activity,
                    getString(R.string.toast_check_google_play_services_failed, errMsg),
                    Toast.LENGTH_LONG).show()
        }

        private fun initGeneral() {
            val general = findPreference(PREF_KEY_CATEGORY_GENERAL) as PreferenceCategory
            val debugPref = findPreference(PREF_KEY_DEBUG)
            val loginPref = findPreference(PREF_KEY_LOGIN)

            if (BuildConfig.DEBUG) {
                debugPref.onPreferenceClickListener = this
            } else {
                general.removePreference(debugPref)
                general.removePreference(findPreference(PREF_KEY_ENABLE_FORCE_TOUCH))
            }

            if (UserState.isLoggedIn()) {
                general.removePreference(loginPref)
            } else {
                loginPref.onPreferenceClickListener = this
            }

            findPreference(PREF_KEY_VERSION).summary =
                    "%s(%d) by CzBiX".format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                REQ_LOGIN -> if (resultCode == Activity.RESULT_OK) {
                    activity.recreate()
                }
            }

            super.onActivityResult(requestCode, resultCode, data)
        }

        private fun toggleReceiveNotifications(turnOn: Boolean): Boolean {
            check(UserState.isLoggedIn()) { "guest can't toggle notifications" }
            task?.dispose()

//            mNotificationsPref.isEnabled = false
            task = RxBus.subscribe<DeviceRegisterEvent> {
                onDeviceRegisterEvent(it)
            }
//            activity.startService(GoogleHelper.getRegistrationIntentToStartService(activity, turnOn))
            return false
        }

        fun onDeviceRegisterEvent(e: DeviceRegisterEvent) {
            if (e.isSuccess) {
//                mNotificationsPref.isChecked = e.isRegister
            } else {
                val resId =if (e.isRegister) {
                    R.string.toast_register_device_failed
                } else {
                    R.string.toast_unregister_device_failed
                }
                Toast.makeText(AppCtx.instance, resId, Toast.LENGTH_LONG).show()
//                mNotificationsPref.isChecked = !e.isRegister
            }
//            mNotificationsPref.isEnabled = true
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            when (preference.key) {
                PREF_KEY_LOGIN -> {
                    startActivityForResult(Intent(activity, LoginActivity::class.java), 0)
                    return true
                }
                PREF_KEY_LOGOUT -> {
                    UserState.logout()
                    activity.recreate()
                    return true
                }
                PREF_KEY_DEBUG -> {
                    startActivity(Intent(activity, DebugActivity::class.java))
                    return true
                }
            }

            return false
        }

        companion object {
            private const val PREF_KEY_CATEGORY_GENERAL = "general"
            private const val PREF_KEY_CATEGORY_USER = "user"
            private const val PREF_KEY_USER_INFO = "user_info"
            private const val PREF_KEY_RECEIVE_NOTIFICATIONS = "receive_notifications"
            private const val PREF_KEY_LOGIN = "login"
            private const val PREF_KEY_DEBUG = "debug"
            private const val PREF_KEY_LOGOUT = "logout"
            private const val PREF_KEY_ENABLE_FORCE_TOUCH = "enable_force_touch"
            private const val PREF_KEY_VERSION = "version"

            private const val REQ_LOGIN = 0
        }
    }
}
