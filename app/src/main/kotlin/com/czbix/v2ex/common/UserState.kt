package com.czbix.v2ex.common

import android.widget.Toast
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.R
import com.czbix.v2ex.dao.ConfigDao
import com.czbix.v2ex.event.BaseEvent.DailyAwardEvent
import com.czbix.v2ex.event.BaseEvent.NewUnreadEvent
import com.czbix.v2ex.eventbus.LoginEvent
import com.czbix.v2ex.helper.RxBus
import com.czbix.v2ex.model.Avatar
import com.czbix.v2ex.network.RequestHelper
import com.czbix.v2ex.parser.MyselfParser
import com.czbix.v2ex.parser.Parser.PageType
import com.czbix.v2ex.util.CrashlyticsUtils
import com.czbix.v2ex.util.ExecutorUtils
import com.czbix.v2ex.util.TrackerUtils
import com.czbix.v2ex.util.UserUtils

object UserState {
    var username: String? = null
        private set
    private var mHasUnread: Boolean = false
    private var mHasAward: Boolean = false

    fun init() {
        RxBus.post(NewUnreadEvent(0))

        username = ConfigDao.get(ConfigDao.KEY_USERNAME, null)
        CrashlyticsUtils.setUserState(isLoggedIn())

        RxBus.subscribe<DailyAwardEvent> {
            mHasAward = it.mHasAward
        }
    }

    fun handleInfo(info: MyselfParser.MySelfInfo?, pageType: PageType) {
        if (info == null) {
            logout()
            return
        }

        if (pageType === PageType.Topic) {
            return
        }

        if (info.unread > 0) {
            mHasUnread = true
            RxBus.post(NewUnreadEvent(info.unread))
        }
        if (pageType === PageType.Tab && info.hasAward != mHasAward) {
            RxBus.post(DailyAwardEvent(info.hasAward))
        }
    }

    fun login(username: String, avatar: Avatar) {
        ConfigDao.put(ConfigDao.KEY_AVATAR, avatar.baseUrl)
        ConfigDao.put(ConfigDao.KEY_USERNAME, username)

        this.username = username
        CrashlyticsUtils.setUserState(true)
        TrackerUtils.setUserId(username)

        AppCtx.eventBus.post(LoginEvent(username))
        ExecutorUtils.execute { UserUtils.checkDailyAward() }
    }

    fun logout() {
        username = null
        RequestHelper.cleanCookies()

        ConfigDao.remove(ConfigDao.KEY_USERNAME)
        ConfigDao.remove(ConfigDao.KEY_AVATAR)

        TrackerUtils.setUserId(null)
        CrashlyticsUtils.setUserState(false)

        ExecutorUtils.runInUiThread {
            Toast.makeText(AppCtx.instance, R.string.toast_has_sign_out,
                    Toast.LENGTH_LONG).show()
        }
        AppCtx.eventBus.post(LoginEvent())
    }

    fun isLoggedIn(): Boolean {
        return username != null
    }

    fun hasUnread(): Boolean {
        return mHasUnread
    }

    fun clearUnread() {
        mHasUnread = false
        RxBus.post(NewUnreadEvent(0))
    }

    fun hasAward(): Boolean {
        return mHasAward
    }
}
